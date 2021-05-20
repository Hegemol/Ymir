package org.season.ymir.common.utils;

import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.core.balance.LoadBalance;
import org.season.ymir.spi.loader.ExtensionLoader;

import java.util.List;

/**
 * 负载均衡工具
 *
 * @author KevinClair
 **/
public class LoadBalanceUtils {

    /**
     * 负载均衡工具
     *
     * @param serviceBeans 服务Bean集合
     * @param type         负载均衡类型
     * @param ip           服务ip地址
     * @return {@link ServiceBean}
     */
    public static ServiceBean selector(final List<ServiceBean> serviceBeans, final String type, final String ip) {
        LoadBalance loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getLoader(type);
        return loadBalance.load(serviceBeans, ip);
    }
}
