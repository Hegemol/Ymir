package org.season.ymir.client.process;

import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.season.ymir.client.annotation.YmirReference;
import org.season.ymir.client.annotation.YmirService;
import org.season.ymir.client.proxy.YmirClientProxyFactory;
import org.season.ymir.common.constant.CommonConstant;
import org.season.ymir.common.register.ServiceBean;
import org.season.ymir.common.register.ServiceRegister;
import org.season.ymir.common.utils.YmirThreadFactory;
import org.season.ymir.core.cache.YmirServerDiscoveryCache;
import org.season.ymir.core.zookeeper.ZkChildListenerImpl;
import org.season.ymir.core.zookeeper.ZookeeperYmirServerDiscovery;
import org.season.ymir.server.YmirNettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 服务导出
 *
 * @author KevinClair
 */
public class DefaultServiceExportProcessor implements ApplicationListener<ContextRefreshedEvent> {

    private final AtomicBoolean flag = new AtomicBoolean(false);

    private static final Logger logger = LoggerFactory.getLogger(DefaultServiceExportProcessor.class);

    private ExecutorService executorService;
    private ServiceRegister serviceRegister;
    private YmirNettyServer nettyServer;
    private YmirClientProxyFactory proxyFactory;

    public DefaultServiceExportProcessor(ServiceRegister serviceRegister, YmirNettyServer nettyServer, YmirClientProxyFactory proxyFactory) {
        this.executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new YmirThreadFactory("service-export-"));
        this.serviceRegister = serviceRegister;
        this.nettyServer = nettyServer;
        this.proxyFactory = proxyFactory;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (!flag.compareAndSet(false, true)) {
            return;
        }

        ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
        executorService.execute(() -> handler(applicationContext));
    }

    private void handler(ApplicationContext applicationContext) {
        // 上传注册信息
        registerService(applicationContext);
        // 注入服务信息
        referenceService(applicationContext);
    }

    private void registerService(ApplicationContext context) {
        Map<String, Object> beans = context.getBeansWithAnnotation(YmirService.class);
        if (beans.size() > 0) {
            for (Object obj : beans.values()) {
                try {
                    Class<?> clazz = obj.getClass();
                    ServiceBean serviceBean;
                    YmirService service = clazz.getAnnotation(YmirService.class);
                    if (StringUtils.isNotBlank(service.value())) {
                        serviceBean = new ServiceBean(service.value(), clazz, obj);
                    } else {
                        Class<?>[] interfaces = clazz.getInterfaces();
                        if (interfaces.length > 1) {
                            logger.error("Only one interface class can be inherited, class {} is illegal!", obj.getClass().getName());
                            continue;
                        }
                        Class<?> superInterface = interfaces[0];
                        serviceBean = new ServiceBean(superInterface.getName(), clazz, obj);
                    }
                    // register bean;
                    serviceRegister.registerBean(serviceBean);
                    logger.info("Service {} register success", obj.getClass().getName());
                } catch (Exception e) {
                    logger.error("Service {} register error, error message: {}", obj.getClass().getName(), ExceptionUtils.getStackTrace(e));
                }
            }
            // TODO netty启动
            nettyServer.start();
        }

    }

    private void referenceService(ApplicationContext context) {
        String[] names = context.getBeanDefinitionNames();
        for (String name : names) {
            Class<?> clazz = context.getType(name);
            if (Objects.isNull(clazz)) {
                continue;
            }

            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                YmirReference reference = field.getAnnotation(YmirReference.class);
                if (Objects.isNull(reference)) {
                    continue;
                }

                Class<?> fieldClass = field.getType();
                Object object = context.getBean(name);
                field.setAccessible(true);
                try {
                    field.set(object, proxyFactory.getProxy(fieldClass));
                } catch (IllegalAccessException e) {
                    logger.error("Service reference error, exception:{}", ExceptionUtils.getStackTrace(e));
                }
                YmirServerDiscoveryCache.SERVICE_LIST.add(fieldClass.getName());
            }
        }
        // 注册子节点监听
        if (proxyFactory.getServerDiscovery() instanceof ZookeeperYmirServerDiscovery) {
            ZookeeperYmirServerDiscovery serverDiscovery = (ZookeeperYmirServerDiscovery) proxyFactory.getServerDiscovery();
            ZkClient zkClient = serverDiscovery.getZkClient();
            YmirServerDiscoveryCache.SERVICE_LIST.forEach(name -> {
                String servicePath = CommonConstant.ZK_SERVICE_PATH + CommonConstant.PATH_DELIMITER + name + "/service";
                zkClient.subscribeChildChanges(servicePath, new ZkChildListenerImpl());
            });
            logger.info("subscribe service zk node successfully");
        }
    }
}
