package org.season.ymir.common.register;


import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.common.entity.ServiceBeanCache;
import org.season.ymir.common.exception.RpcException;

import java.util.Objects;

/**
 * 默认的服务注册抽象实现类
 *
 * @author KevinClair
 */
public abstract class DefaultAbstractServiceRegister implements ServiceRegister {

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
        return service.get(name);
    }

    @Override
    public void registerBean(final ServiceBean serviceBean) throws RpcException {
        if (Objects.isNull(serviceBean)) throw new RpcException("parameter can not be empty.");
        serviceBean.setProtocol(protocol);
        try {
            // 实例化对象
            Class<?> classObject = Class.forName(serviceBean.getClazz());
            ServiceBeanCache serviceBeanCache = new ServiceBeanCache(serviceBean.getName(), classObject, classObject.newInstance());
            service.put(serviceBean.getName(), serviceBeanCache);
        } catch (Exception e) {
            throw new RpcException(String.format("Register bean %s error, please check it.", serviceBean.getName()));
        }
    }
}

