package org.season.ymir.core.cache;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.season.ymir.common.entity.ServiceBeanModel;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务发现本地缓存
 *
 * @author KevinClair
 **/
public class YmirServerDiscoveryCache {

    // 本地缓存
    private static final LoadingCache<String, List<ServiceBeanModel>> SERVER_MAP = Caffeine.newBuilder()
            .initialCapacity(10)
            .maximumSize(50000)
            .build(new CacheLoader<String, List<ServiceBeanModel>>() {
                @Nullable
                @Override
                public List<ServiceBeanModel> load(@NonNull String s) throws Exception {
                    return null;
                }
            });

    /**
     * 客户端注入的远程服务service class
     */
    public static final List<String> SERVICE_LIST = new ArrayList<>();

    /**
     * 清空缓存
     *
     * @param serviceName
     */
    public static void remove(String serviceName) {
        SERVER_MAP.invalidate(serviceName);
    }

    /**
     * 本地缓存列表是否为空
     *
     * @param serviceName
     * @return
     */
    public static boolean isEmpty(String serviceName) {
        return CollectionUtils.isEmpty(SERVER_MAP.get(serviceName));
    }

    /**
     * 获取缓存
     *
     * @param serviceName
     * @return
     */
    public static List<ServiceBeanModel> get(String serviceName) {
        return SERVER_MAP.get(serviceName);
    }

    /**
     * 设置缓存
     *
     * @param serviceName
     * @param serviceList
     */
    public static void put(String serviceName, List<ServiceBeanModel> serviceList) {
        SERVER_MAP.put(serviceName, serviceList);
    }
}
