package org.season.ymir.client.proxy;

import org.season.ymir.client.net.NettyNetClient;
import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.common.exception.RpcException;
import org.season.ymir.common.model.YmirRequest;
import org.season.ymir.common.model.YmirResponse;
import org.season.ymir.core.cache.YmirServerDiscoveryCache;
import org.season.ymir.core.handler.LoadBalance;
import org.season.ymir.core.handler.MessageProtocol;
import org.season.ymir.server.YmirServerDiscovery;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * 客户端代理
 *
 * @author KevinClair
 **/
public class YmirClientProxyFactory {

    private YmirServerDiscovery serverDiscovery;

    private NettyNetClient netClient;

    private Map<String, MessageProtocol> supportMessageProtocols;

    private Map<Class<?>, Object> objectCache = new HashMap<>();

    private LoadBalance loadBalance;

    /**
     * 通过Java动态代理获取服务代理类
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getProxy(Class<T> clazz) {
        return (T) objectCache.computeIfAbsent(clazz, clz ->
                Proxy.newProxyInstance(clz.getClassLoader(), new Class[]{clz}, new ClientInvocationHandler(clz))
        );
    }


    private class ClientInvocationHandler implements InvocationHandler {

        private Class<?> clazz;

        public ClientInvocationHandler(Class<?> clazz) {
            this.clazz = clazz;
        }


        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (method.getName().equals("toString")) {
                return proxy.toString();
            }

            if (method.getName().equals("hashCode")) {
                return 0;
            }
            // 1.获得服务信息
            String serviceName = clazz.getName();
            List<ServiceBean> services = getServiceList(serviceName);
            ServiceBean service = loadBalance.load(services);
            // 2.构造request对象
            YmirRequest request = new YmirRequest();
            request.setRequestId(UUID.randomUUID().toString());
            request.setServiceName(service.getName());
            request.setMethod(method.getName());
            request.setParameters(args);
            request.setParameterTypes(method.getParameterTypes());
            // 3.协议层编组
            MessageProtocol messageProtocol = supportMessageProtocols.get(service.getProtocol());
            YmirResponse response = netClient.sendRequest(request, service, messageProtocol);
//            byte[] reqData = messageProtocol.marshallingRequest(request);
//            // 4. 调用网络层发送请求
//            byte[] respData = netClient.sendRequest(reqData, service);
//
//            // 5. 解组响应消息
//            YmirResponse response = messageProtocol.unmarshallingResponse(respData);
            if (Objects.isNull(response)){
                throw new RpcException("the response is null");
            }
            // 6.结果处理
            if (response.getException() != null) {
                return response.getException();
            }

            return !Objects.isNull(response.getException())?response.getException():response.getReturnValue();
        }
    }

    /**
     * 根据服务名获取可用的服务地址列表
     * @param serviceName
     * @return
     */
    private List<ServiceBean> getServiceList(String serviceName) {
        List<ServiceBean> services;
//        synchronized (serviceName){
            if (YmirServerDiscoveryCache.isEmpty(serviceName)) {
                // TODO 此处需要修改；
                services = serverDiscovery.findServiceList(serviceName);
                if (CollectionUtils.isEmpty(services)) {
                    throw new RpcException("No provider available for service "+ serviceName);
                }
                YmirServerDiscoveryCache.put(serviceName, services);
            } else {
                services = YmirServerDiscoveryCache.get(serviceName);
            }
//        }
        return services;
    }

    public YmirServerDiscovery getServerDiscovery() {
        return serverDiscovery;
    }

    public void setServerDiscovery(YmirServerDiscovery serverDiscovery) {
        this.serverDiscovery = serverDiscovery;
    }

    public NettyNetClient getNetClient() {
        return netClient;
    }

    public void setNetClient(NettyNetClient netClient) {
        this.netClient = netClient;
    }

    public Map<String, MessageProtocol> getSupportMessageProtocols() {
        return supportMessageProtocols;
    }

    public void setSupportMessageProtocols(Map<String, MessageProtocol> supportMessageProtocols) {
        this.supportMessageProtocols = supportMessageProtocols;
    }

    public Map<Class<?>, Object> getObjectCache() {
        return objectCache;
    }

    public void setObjectCache(Map<Class<?>, Object> objectCache) {
        this.objectCache = objectCache;
    }

    public LoadBalance getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }
}
