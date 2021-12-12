package org.season.ymir.common.register;


import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.common.entity.ServiceBeanCache;
import org.season.ymir.common.entity.ServiceBeanEvent;
import org.season.ymir.common.exception.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * 默认的服务注册抽象实现类
 *
 * @author KevinClair
 */
public abstract class DefaultAbstractServiceRegister implements ServiceRegister {

    private static final Logger logger = LoggerFactory.getLogger(DefaultAbstractServiceRegister.class);

    /**
     * 协议
     */
    protected String protocol;
    /**
     * 端口
     */
    protected Integer port;

    // 本地缓存
    private LoadingCache<String, ServiceBeanCache> service = Caffeine.newBuilder()
            .initialCapacity(10)
            .maximumSize(50000)
            .build(new CacheLoader<String, ServiceBeanCache>() {
                @Nullable
                @Override
                public ServiceBeanCache load(@NonNull String s) throws Exception {
                    return null;
                }
            });

    @Override
    public ServiceBeanCache getBean(final String name) throws RpcException {
        return Optional.ofNullable(service.get(name)).orElseGet(() -> getIfNoCache(name));
    }

    @Override
    public void registerBean(final ServiceBean serviceBean) throws RpcException {
        if (Objects.isNull(serviceBean)) throw new RpcException("parameter can not be empty.");
        serviceBean.setProtocol(protocol);
        try {
            // 实例化对象
            Class<?> classObject = Class.forName(serviceBean.getClazz());
            ServiceBeanCache serviceBeanCache = new ServiceBeanCache(serviceBean.getName(), classObject, classObject.newInstance(), serviceBean.getFilter());
            service.put(serviceBean.getName(), serviceBeanCache);
        } catch (Exception e) {
            throw new RpcException(String.format("Register bean %s error, please check it.", serviceBean.getName()));
        }
        // 通过注册中心注册实例
        ServiceBeanEvent exportEventModel = new ServiceBeanEvent();
        exportEventModel.setName(serviceBean.getName());
        exportEventModel.setProtocol(protocol);
        exportEventModel.setAddress(serviceBean.getAddress());
        exportEventModel.setWeight(serviceBean.getWeight());
        exportEventModel.setGroup(serviceBean.getGroup());
        exportEventModel.setVersion(serviceBean.getVersion());
        try {
            // 服务发布至注册中心
            exportService(serviceBean, exportEventModel);
        } catch (Exception e) {
            logger.error("Export service to register center error, error message:{}", ExceptionUtils.getStackTrace(e));
            throw new RpcException(String.format("Export service %s to register center error", serviceBean.getName()));
        }
        // 发布事件
        publishEvent(exportEventModel);
    }

    /**
     * 缓存中不存在时，从注册中心获取
     *
     * @param name 服务名
     * @return 服务bean的缓存实例 {@link ServiceBeanCache}
     */
    protected abstract ServiceBeanCache getIfNoCache(final String name);

    /**
     * 发布服务导出事件
     *
     * @param serviceBeanEvent 服务bean事件模型
     */
    protected abstract void publishEvent(final ServiceBeanEvent serviceBeanEvent);

    /**
     * 服务导出
     *
     * @param model            服务bean
     * @param exportEventModel 事件导出模型
     */
    protected abstract void exportService(final ServiceBean model, final ServiceBeanEvent exportEventModel) throws Exception;
}

