package org.season.ymir.common.register;


import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

/**
 * 默认的服务注册抽象实现类
 *
 * @author KevinClair
 */
public abstract class DefaultAbstractServiceRegister implements ServiceRegister {

    protected String protocol;
    protected Integer port;
    /**
     * 权重
     */
    protected Integer weight;

    // 本地缓存
    private LoadingCache<String, ServiceBean> service = Caffeine.newBuilder()
            .initialCapacity(10)
            .maximumSize(50000)
            .build(new CacheLoader<String, ServiceBean>() {
                @Nullable
                @Override
                public ServiceBean load(@NonNull String s) throws Exception {
                    return null;
                }
            });

    @Override
    public ServiceBean getBean(final String name) throws Exception {
        return service.get(name);
    }

    @Override
    public void registerBean(final ServiceBean bean) throws Exception {
        if (Objects.isNull(bean)) throw new IllegalArgumentException("parameter can not be empty.");
        service.put(bean.getName(), bean);
    }
}

