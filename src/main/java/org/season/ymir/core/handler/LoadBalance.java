package org.season.ymir.core.handler;

import org.season.ymir.common.entity.ServiceBean;

import java.util.List;

/**
 * 负载均衡
 *
 * @author KevinClair
 **/
public interface LoadBalance {

    /**
     * 负载均衡器
     *
     * @param services 服务列表
     * @return {@link ServiceBean}
     */
    ServiceBean load(List<ServiceBean> services);
}
