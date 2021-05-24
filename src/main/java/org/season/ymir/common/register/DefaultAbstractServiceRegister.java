package org.season.ymir.common.register;


import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.common.entity.ServiceBeanCache;

import java.net.InetAddress;
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
    public ServiceBeanCache getBean(final String name) throws Exception {
        return service.get(name);
    }

    @Override
    public void registerBean(final ServiceBean serviceBean) throws Exception {
        if (Objects.isNull(serviceBean)) throw new IllegalArgumentException("parameter can not be empty.");
        String host = InetAddress.getLocalHost().getHostAddress();
        String address = host + ":" + port;
        serviceBean.setAddress(address);
        serviceBean.setProtocol(StringUtils.isBlank(serviceBean.getProtocol()) ? protocol : serviceBean.getProtocol());
        service.put(serviceBean.getName(), new ServiceBeanCache(serviceBean.getName(), serviceBean.getClazz(), serviceBean.getBean()));
    }
}

