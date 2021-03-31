package org.season.ymir.client.process;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.season.ymir.client.annotation.YmirService;
import org.season.ymir.common.register.ServiceBean;
import org.season.ymir.common.register.ServiceRegister;
import org.season.ymir.common.utils.YmirThreadFactory;
import org.season.ymir.server.YmirNettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;
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

    public DefaultServiceExportProcessor(ServiceRegister serviceRegister, YmirNettyServer nettyServer) {
        this.executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new YmirThreadFactory("service-export-"));
        this.serviceRegister = serviceRegister;
        this.nettyServer = nettyServer;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if (!flag.compareAndSet(false, true)) {
            return;
        }

        ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
        executorService.execute(() -> handler(applicationContext));
    }

    private void handler(ApplicationContext applicationContext){
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
                        if (interfaces.length > 1){
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
            nettyServer.start();
        }

    }

    private void referenceService(ApplicationContext applicationContext) {

    }
}
