package org.hegemol.ymir.core;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.hegemol.ymir.client.proxy.ClientProxyFactory;
import org.hegemol.ymir.common.entity.ServiceBean;
import org.hegemol.ymir.common.exception.RpcException;
import org.hegemol.ymir.common.register.ServiceRegister;
import org.hegemol.ymir.core.annotation.Reference;
import org.hegemol.ymir.core.annotation.Service;
import org.hegemol.ymir.core.generic.GenericService;
import org.hegemol.ymir.core.property.ConfigurationProperty;
import org.hegemol.ymir.server.NettyServer;
import org.hegemol.ymir.server.discovery.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * 服务导出
 *
 * @author KevinClair
 */
public class ServiceExportProcessor implements ApplicationListener<ContextRefreshedEvent> {

    private final AtomicBoolean flag = new AtomicBoolean(false);

    private static final Logger logger = LoggerFactory.getLogger(ServiceExportProcessor.class);

    private ServiceRegister serviceRegister;
    private NettyServer nettyServer;
    private ClientProxyFactory proxyFactory;
    private ConfigurationProperty property;
    private ServiceDiscovery serviceDiscovery;

    public ServiceExportProcessor(ServiceRegister serviceRegister, NettyServer nettyServer, ClientProxyFactory proxyFactory, ConfigurationProperty property, ServiceDiscovery serviceDiscovery) {
        this.serviceRegister = serviceRegister;
        this.nettyServer = nettyServer;
        this.proxyFactory = proxyFactory;
        this.property = property;
        this.serviceDiscovery = serviceDiscovery;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (!flag.compareAndSet(false, true)) {
            return;
        }
        ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
        ThreadPoolFactory.execute(() -> handler(applicationContext));
    }

    private void handler(ApplicationContext applicationContext) {
        try {
            // 解析地址信息
            String host = InetAddress.getLocalHost().getHostAddress();
            String address = host + ":" + property.getPort();
            // 注册服务
            registerService(applicationContext, address);
            // 引用服务
            referenceService(applicationContext, address);
        } catch (Exception e) {
            logger.error("Ymir deploy error, error message:{}", ExceptionUtils.getStackTrace(e));
        }
    }

    private void registerService(ApplicationContext context, String address) {
        Map<String, Object> beans = context.getBeansWithAnnotation(Service.class);

        if (beans.size() > 0) {
            for (Object obj : beans.values()) {
                try {
                    Class<?> clazz = obj.getClass();
                    if (!clazz.getName().startsWith(property.getScanPackages())){
                        continue;
                    }
                    Service service = clazz.getAnnotation(Service.class);
                    // 如果不需要注册，跳过
                    if (!service.register()) {
                        continue;
                    }
                    Class<?>[] interfaces = clazz.getInterfaces();
                    if (interfaces.length > 1) {
                        logger.error("Only one interface class can be inherited, class {} is illegal!", obj.getClass().getName());
                        continue;
                    }
                    ServiceBean serviceBean = new ServiceBean(interfaces[0].getName(), clazz.getName(), address, service.weight(), service.group(), service.version(), service.filter(), service.fictitiousInstance());
                    // register bean;
                    serviceRegister.registerBean(serviceBean);
                    logger.info("Service {} register success", obj.getClass().getName());
                } catch (Exception e) {
                    logger.error("Service {} register error, error message: {}", obj.getClass().getName(), ExceptionUtils.getStackTrace(e));
                }
            }
            // netty服务端启动
            nettyServer.start();
        }

    }

    private void referenceService(ApplicationContext context, String address) {
        String[] names = context.getBeanDefinitionNames();
        List<String> serviceList = new ArrayList<>();
        for (String name : names) {
            Class<?> clazz = context.getType(name);
            if (Objects.isNull(clazz)) {
                continue;
            }

            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                Reference reference = field.getAnnotation(Reference.class);
                if (Objects.isNull(reference)) {
                    continue;
                }
                Class<?> fieldClass = field.getType();
                if(reference.check()) {
                    if (!fieldClass.getName().equals(GenericService.class.getName())) {
                        // do nothing
                        try {
                            final List<ServiceBean> serviceBeans = serviceDiscovery.findServiceList(fieldClass.getName());
                            if (Objects.isNull(serviceBeans) || serviceBeans.isEmpty()) {
                                throw new RpcException(String.format("No provider available for service %s", fieldClass.getName()));
                            }
                        } catch (Exception e) {
                            logger.error("Check service error:{}", ExceptionUtils.getStackTrace(e));
                            throw new RpcException(e);
                        }
                    }
                }
                Object object = context.getBean(name);
                field.setAccessible(true);
                try {
                    // 设置代理对象
                    field.set(object, proxyFactory.getProxy(fieldClass, reference));
                } catch (IllegalAccessException e) {
                    logger.error("Service reference error, exception:{}", ExceptionUtils.getStackTrace(e));
                    throw new RpcException(e);
                }
                serviceList.add(fieldClass.getName());
            }
        }
        // 注册子节点监听
        serviceDiscovery.listener(serviceList, address);

        logger.info("Subscribe service successfully");
    }
}
