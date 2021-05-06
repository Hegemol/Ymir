package org.season.ymir.core.balance;

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
     * @param address  服务注册地址
     * @return {@link ServiceBean}
     */
    ServiceBean load(List<ServiceBean> services, String address);
}
