package org.hegemol.ymir.server.discovery;

import org.hegemol.ymir.common.entity.ServiceBean;

import java.util.List;

/**
 * 服务发现
 *
 * @author KevinClair
 **/
public interface ServiceDiscovery {

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

    /**
     * 服务监听
     *
     * @param serviceList 服务列表
     * @param address     地址
     */
    void listener(List<String> serviceList, String address);
}
