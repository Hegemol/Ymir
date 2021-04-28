package org.season.ymir.core.discovery;

import org.season.ymir.common.entity.ServiceBean;

import java.util.List;

/**
 * 服务发现
 *
 * @author KevinClair
 **/
public interface YmirServiceDiscovery {

    /**
     * 服务发现
     *
     * @param name
     * @return
     */
    List<ServiceBean> findServiceList(String name) throws Exception;

    /**
     * 清空
     *
     * @param serviceName 名称
     */
    void remove(String serviceName);

    /**
     * 实例列表是否为空
     *
     * @param serviceName 服务名称
     * @return
     */
    boolean isEmpty(String serviceName);

    /**
     * 获取实例列表
     *
     * @param serviceName 服务名
     * @return {@link ServiceBean}
     */
    List<ServiceBean> get(String serviceName);

    /**
     * 添加实例
     *
     * @param serviceName 服务名称
     * @param serviceList 实例列表
     */
    void put(String serviceName, List<ServiceBean> serviceList);
}
