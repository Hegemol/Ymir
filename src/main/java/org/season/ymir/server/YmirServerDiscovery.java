package org.season.ymir.server;

import org.season.ymir.common.entity.ServiceBean;

import java.util.List;

/**
 * 服务发现
 *
 * @author KevinClair
 **/
public interface YmirServerDiscovery {

    /**
     * 服务发现
     *
     * @param name
     * @return
     */
    List<ServiceBean> findServiceList(String name) throws Exception;
}
