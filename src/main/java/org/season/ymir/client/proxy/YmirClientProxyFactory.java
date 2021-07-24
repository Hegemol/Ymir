package org.season.ymir.client.proxy;

import org.season.ymir.client.YmirNettyClient;
import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.common.exception.RpcException;
import org.season.ymir.common.model.YmirRequest;
import org.season.ymir.common.model.YmirResponse;
import org.season.ymir.common.utils.LoadBalanceUtils;
import org.season.ymir.core.annotation.YmirReference;
import org.season.ymir.server.discovery.YmirServiceDiscovery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端代理
 *
 * @author KevinClair
 **/
public class YmirClientProxyFactory {

    private YmirServiceDiscovery serviceDiscovery;

    private YmirNettyClient netClient;

    private Map<Class<?>, Object> objectCache = new ConcurrentHashMap<>();

    /**
     * 通过Java动态代理获取服务代理类
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getProxy(Class<T> clazz, YmirReference reference) {
        return (T) objectCache.computeIfAbsent(clazz, clz ->
                Proxy.newProxyInstance(clz.getClassLoader(), new Class[]{clz}, new ClientInvocationHandler(clz, reference))
        );
    }


    private class ClientInvocationHandler implements InvocationHandler {

        private Class<?> clazz;

        private YmirReference reference;

        public ClientInvocationHandler(Class<?> clazz, YmirReference reference) {
            this.clazz = clazz;
            this.reference = reference;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 1.获得服务信息
            String serviceName = clazz.getName();
            List<ServiceBean> services = serviceDiscovery.findServiceList(serviceName);
            // TODO 此处address地址
            ServiceBean service = LoadBalanceUtils.selector(services, reference.loadBalance(), "");
            // 2.构造request对象
            YmirRequest request = new YmirRequest();
            request.setRequestId(UUID.randomUUID().toString());
            request.setServiceName(service.getName());
            request.setMethod(method.getName());
            request.setParameters(args);
            request.setParameterTypes(method.getParameterTypes());
            request.setTimeout(reference.timeout());
            request.setRetries(reference.retries());
            // 3.发送请求
            YmirResponse response = netClient.sendRequest(request, service);
            if (Objects.isNull(response)){
                throw new RpcException("the response is null");
            }
            // 4.结果处理
            return !Objects.isNull(response.getException())?response.getException():response.getReturnValue();
        }
    }

    public YmirClientProxyFactory(YmirServiceDiscovery serviceDiscovery, YmirNettyClient netClient) {
        this.serviceDiscovery = serviceDiscovery;
        this.netClient = netClient;
    }

    public YmirClientProxyFactory() {
    }
}
