package org.hegemol.ymir.client.proxy;

import io.netty.channel.Channel;
import org.hegemol.ymir.client.NettyChannelManager;
import org.hegemol.ymir.client.NettyClient;
import org.hegemol.ymir.client.RequestFutureManager;
import org.hegemol.ymir.common.base.MessageTypeEnum;
import org.hegemol.ymir.common.base.SerializationTypeEnum;
import org.hegemol.ymir.common.constant.CommonConstant;
import org.hegemol.ymir.common.entity.ServiceBean;
import org.hegemol.ymir.common.exception.RpcException;
import org.hegemol.ymir.common.exception.RpcTimeoutException;
import org.hegemol.ymir.common.model.InvocationMessage;
import org.hegemol.ymir.common.model.InvocationMessageWrap;
import org.hegemol.ymir.common.model.Request;
import org.hegemol.ymir.common.model.Response;
import org.hegemol.ymir.common.utils.ClassUtil;
import org.hegemol.ymir.common.utils.LoadBalanceUtils;
import org.hegemol.ymir.core.annotation.Reference;
import org.hegemol.ymir.core.context.RpcContext;
import org.hegemol.ymir.core.filter.DefaultFilterChain;
import org.hegemol.ymir.core.generic.GenericService;
import org.hegemol.ymir.core.property.ConfigurationProperty;
import org.hegemol.ymir.server.discovery.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 客户端代理
 *
 * @author KevinClair
 **/
public class ClientProxyFactory {

    private ServiceDiscovery serviceDiscovery;

    private NettyClient netClient;

    private ConfigurationProperty property;

    private Map<Class<?>, Object> objectCache = new ConcurrentHashMap<>();

    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);

    public ClientProxyFactory(ServiceDiscovery serviceDiscovery, NettyClient netClient, ConfigurationProperty property) {
        this.serviceDiscovery = serviceDiscovery;
        this.netClient = netClient;
        this.property = property;
    }

    public ClientProxyFactory() {
    }

    /**
     * 通过Java动态代理获取服务代理类
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getProxy(Class<T> clazz, Reference reference) {
        return (T) objectCache.computeIfAbsent(clazz, clz ->
                Proxy.newProxyInstance(clz.getClassLoader(), new Class[]{clz}, new ClientInvocationHandler(clz, reference))
        );
    }


    private class ClientInvocationHandler implements InvocationHandler {

        private final Logger logger = LoggerFactory.getLogger(ClientInvocationHandler.class);

        private Class<?> clazz;

        private Reference reference;

        public ClientInvocationHandler(Class<?> clazz, Reference reference) {
            this.clazz = clazz;
            this.reference = reference;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 1.获得服务信息
            String className = clazz.getName();
            String serviceName = clazz.getName();
            Object[] parameters = args;
            Class<?>[] parameterTypes = method.getParameterTypes();
            String methodName = method.getName();
            // 泛化调用实现
            if (className.equals(GenericService.class.getName()) && method.getName().equals("invoke")) {
                // 如果是泛化调用，根据参数查找服务名;
                serviceName = (String) args[0];
                methodName = (String) args[1];
                Class<?>[] paramTypes = null;
                if (Objects.nonNull(args[2])) {
                    // 解析请求参数类型
                    String[] paramTypesStringArray = (String[]) args[2];
                    if (Objects.nonNull(paramTypesStringArray) && paramTypesStringArray.length > 0) {
                        paramTypes = new Class[paramTypesStringArray.length];
                        for (int i = 0; i < paramTypesStringArray.length; i++) {
                            paramTypes[i] = ClassUtil.resolveClass(paramTypesStringArray[i]);
                        }
                    }
                }
                parameterTypes = paramTypes;
                parameters = (Object[]) args[3];
            }
            List<ServiceBean> services = serviceDiscovery.findServiceList(serviceName);
            // TODO 此处address地址
            ServiceBean service = LoadBalanceUtils.selector(services, reference.loadBalance(), reference.url(), "");
            // 2.构造request对象
            InvocationMessageWrap<Request> invocationMessageWrap = new InvocationMessageWrap();
            invocationMessageWrap.setType(MessageTypeEnum.SERVICE_REQUEST);
            InvocationMessage<Request> requestInvocationMessage = new InvocationMessage<>();
            requestInvocationMessage.setRetries(reference.retries());
            requestInvocationMessage.setTimeout(reference.timeout());
            Request request = new Request();
            request.setServiceName(service.getName());
            request.setMethod(methodName);
            request.setParameters(parameters);
            request.setParameterTypes(parameterTypes);
            requestInvocationMessage.setBody(request);
            // 设置Filter
            requestInvocationMessage.setHeaders(new HashMap<String, String>() {{
                put(CommonConstant.FILTER_FROM_HEADERS, reference.filter());
            }});
            invocationMessageWrap.setData(requestInvocationMessage);
            invocationMessageWrap.setSerial(SerializationTypeEnum.getType(property.getSerial()));
            invocationMessageWrap.setRequestId(ATOMIC_INTEGER.getAndIncrement());
            // 3.发送请求
            CompletableFuture<Response> responseFuture = sendRequest(invocationMessageWrap, service.getAddress());
            // 4.同步请求和异步请求结果处理，如果是同步请求，请求返回结果为null，需要从RpcContext中获取Future结果
            if (reference.async() || RpcContext.getContext().getAttachments().get("async").equals("true")) {
                RpcContext.setFuture(responseFuture.thenApply(future -> future.getResult()));
                return null;
            }
            Response response = this.getResponse(responseFuture, invocationMessageWrap);
            if (Objects.isNull(response)) {
                throw new RpcException("the response is null");
            }
            return response.getResult();
        }

        /**
         * 发送请求
         *
         * @param rpcRequest 请求参数
         * @param address    服务地址
         * @return {@link Response}
         */
        private CompletableFuture<Response> sendRequest(InvocationMessageWrap<Request> rpcRequest, String address) {

            if (NettyChannelManager.contains(address)) {
                return this.sendRequestByChannel(rpcRequest, NettyChannelManager.get(address));
            }
            synchronized (address) {
                if (NettyChannelManager.contains(address)) {
                    return this.sendRequestByChannel(rpcRequest, NettyChannelManager.get(address));
                }
                // 建立客户端
                netClient.connect(address);
                return this.sendRequestByChannel(rpcRequest, NettyChannelManager.get(address));
            }
        }

        private Response getResponse(CompletableFuture<Response> responseFuture, InvocationMessageWrap<Request> request) {
            InvocationMessage<Request> data = request.getData();
            try {
                // 等待响应
                return responseFuture.get(data.getTimeout(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException exception) {
                throw new RpcTimeoutException(String.format("Invoke remote method %s timeout with %s ms", String.join("#", data.getBody().getServiceName(), data.getBody().getMethod()), data.getTimeout()));
            } catch (Exception e) {
                throw new RpcException(e);
            } finally {
                RequestFutureManager.remove(request.getRequestId());
            }
        }

        private CompletableFuture<Response> sendRequestByChannel(InvocationMessageWrap<Request> request, Channel channel) {
            CompletableFuture<InvocationMessage<Response>> completableFuture = new CompletableFuture<>();
            InvocationMessage<Request> data = request.getData();
            new DefaultFilterChain(new ArrayList<>(Arrays.asList(data.getHeaders().get(CommonConstant.FILTER_FROM_HEADERS).split(","))), CommonConstant.SERVICE_CONSUMER_SIDE).execute(data);
            channel.writeAndFlush(request).addListener(future -> {
                if (future.isSuccess()) {
                    RequestFutureManager.putTask(request.getRequestId(), completableFuture);
                }
            });
            return completableFuture.thenApply(res -> res.getBody());
        }
    }


}
