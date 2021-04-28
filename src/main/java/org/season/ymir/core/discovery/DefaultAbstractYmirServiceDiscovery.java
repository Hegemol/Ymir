package org.season.ymir.core.discovery;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.season.ymir.common.entity.ServiceBean;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 服务发现本地缓存
 *
 * @author KevinClair
 **/
public abstract class DefaultAbstractYmirServiceDiscovery implements YmirServiceDiscovery {

    // 本地缓存
    private static final LoadingCache<String, List<ServiceBean>> SERVER_MAP = Caffeine.newBuilder()
            .initialCapacity(10)
            .maximumSize(50000)
            .build(new CacheLoader<String, List<ServiceBean>>() {
                @Nullable
                @Override
                public List<ServiceBean> load(@NonNull String s) throws Exception {
                    return null;
                }
            });

    @Override
    public void remove(String serviceName) {
        SERVER_MAP.invalidate(serviceName);
    }

    @Override
    public boolean isEmpty(String serviceName) {
        return CollectionUtils.isEmpty(SERVER_MAP.get(serviceName));
    }

    @Override
    public List<ServiceBean> get(String serviceName) {
        return SERVER_MAP.get(serviceName);
    }

    @Override
    public void put(String serviceName, List<ServiceBean> serviceList) {
        SERVER_MAP.put(serviceName, serviceList);
    }

    @Override
    public List<ServiceBean> findServiceList(String name) throws Exception {
        return SERVER_MAP.get(name);
    }
}
