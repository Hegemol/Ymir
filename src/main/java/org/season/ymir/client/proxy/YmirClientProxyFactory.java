package org.season.ymir.client.proxy;

import org.season.ymir.client.YmirNettyClient;
import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.common.exception.RpcException;
import org.season.ymir.common.model.YmirRequest;
import org.season.ymir.common.model.YmirResponse;
import org.season.ymir.common.utils.LoadBalanceUtils;
import org.season.ymir.core.property.YmirConfigurationProperty;
import org.season.ymir.core.protocol.MessageProtocol;
import org.season.ymir.server.discovery.YmirServiceDiscovery;
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

    private YmirServiceDiscovery serviceDiscovery;

    private YmirNettyClient netClient;

    private MessageProtocol protocol;

    private YmirConfigurationProperty property;

    private Map<Class<?>, Object> objectCache = new HashMap<>();

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
            // 1.获得服务信息
            String serviceName = clazz.getName();
            List<ServiceBean> services = getServiceList(serviceName);
            // TODO 此处address地址
            ServiceBean service = LoadBalanceUtils.selector(services, "", "");
            // 2.构造request对象
            YmirRequest request = new YmirRequest();
            request.setRequestId(UUID.randomUUID().toString());
            request.setServiceName(service.getName());
            request.setMethod(method.getName());
            request.setParameters(args);
            request.setParameterTypes(method.getParameterTypes());
            request.setTimeout(property.getTimeout());
            // 3.发送请求
            YmirResponse response = netClient.sendRequest(request, service, protocol);
            if (Objects.isNull(response)){
                throw new RpcException("the response is null");
            }
            // 4.结果处理
            return !Objects.isNull(response.getException())?response.getException():response.getReturnValue();
        }
    }

    /**
     * 根据服务名获取可用的服务地址列表
     * @param serviceName
     * @return
     */
    private List<ServiceBean> getServiceList(String serviceName) throws Exception {
        List<ServiceBean> services;
        synchronized (serviceName){
            if (serviceDiscovery.isEmpty(serviceName)) {
                services = serviceDiscovery.findServiceList(serviceName);
                if (CollectionUtils.isEmpty(services)) {
                    throw new RpcException("No provider available for service "+ serviceName);
                }
                serviceDiscovery.put(serviceName, services);
            } else {
                services = serviceDiscovery.get(serviceName);
            }
        }
        return services;
    }

    public YmirServiceDiscovery getServiceDiscovery() {
        return serviceDiscovery;
    }

    public YmirClientProxyFactory(YmirServiceDiscovery serviceDiscovery, YmirNettyClient netClient, MessageProtocol protocol, YmirConfigurationProperty property) {
        this.serviceDiscovery = serviceDiscovery;
        this.netClient = netClient;
        this.protocol = protocol;
        this.property = property;
    }

    public YmirClientProxyFactory() {
    }
}
