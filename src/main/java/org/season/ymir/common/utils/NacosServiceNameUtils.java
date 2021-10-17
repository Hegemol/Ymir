package org.season.ymir.common.utils;

import org.season.ymir.common.constant.CommonConstant;

/**
 * nacos服务注册时的ServiceName工具类
 *
 * @author KevinClair
 **/
public class NacosServiceNameUtils {

    /**
     * 拼装nacos路径
     *
     * @param side        使用方，分为consumer和provider
     * @param serviceName 接口服务名，全路径。
     * @param version     服务版本号
     * @param group       服务组
     * @return nacos实例名
     */
    public static String buildService(String side, String serviceName, String version, String group){
        return String.join(CommonConstant.INSTANCE_DELIMITER, side, serviceName, group, version);
    }
}