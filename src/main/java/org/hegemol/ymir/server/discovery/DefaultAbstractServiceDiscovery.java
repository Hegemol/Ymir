package org.hegemol.ymir.server.discovery;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.hegemol.ymir.common.entity.ServiceBean;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 服务发现本地缓存
 *
 * @author KevinClair
 **/
public abstract class DefaultAbstractServiceDiscovery implements ServiceDiscovery {

    // 本地缓存
    private static final Cache<String, List<ServiceBean>> SERVER_MAP = Caffeine.newBuilder()
            .initialCapacity(10)
            .maximumSize(50000)
            .build();

    @Override
    public void remove(String serviceName) {
        SERVER_MAP.invalidate(serviceName);
    }

    @Override
    public boolean isEmpty(String serviceName) {
        return CollectionUtils.isEmpty(SERVER_MAP.getIfPresent(serviceName));
    }

    @Override
    public List<ServiceBean> get(String serviceName) {
        return SERVER_MAP.getIfPresent(serviceName);
    }

    @Override
    public void put(String serviceName, List<ServiceBean> serviceList) {
        handleClient(serviceList);
        SERVER_MAP.put(serviceName, serviceList);
    }

    @Override
    public List<ServiceBean> findServiceList(String name) throws Exception {
        List<ServiceBean> serviceList = SERVER_MAP.getIfPresent(name);
        if (!CollectionUtils.isEmpty(serviceList)) {
            return serviceList;
        }
        return findServiceListByRegisterCenter(name);
    }

    /**
     * 客户端处理方法，当初始化客户端或者服务端重启时，调用此方法；
     *
     * @param serviceList 服务列表
     */
    protected abstract void handleClient(List<ServiceBean> serviceList);

    /**
     * 从注册中心读取服务列表
     *
     * @param name 服务名
     * @return 服务列表
     */
    protected abstract List<ServiceBean> findServiceListByRegisterCenter(String name) throws Exception;
}
