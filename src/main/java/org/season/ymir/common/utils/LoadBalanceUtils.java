package org.season.ymir.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.common.exception.RpcException;
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
     * @param url          服务直连url
     * @param address      服务地址
     * @return {@link ServiceBean}
     */
    public static ServiceBean selector(final List<ServiceBean> serviceBeans, final String type, final String url, final String address) {
        if (StringUtils.isNotBlank(url)){
            return serviceBeans.stream().filter(each -> each.getAddress().equals(url)).findFirst().orElseThrow(() -> new RpcException(String.format("No provider available for service %s from url %s", serviceBeans.get(0).getName(), url)));
        }
        LoadBalance loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getLoader(type);
        return loadBalance.load(serviceBeans, address);
    }
}
