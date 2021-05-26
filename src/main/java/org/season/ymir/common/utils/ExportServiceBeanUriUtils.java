package org.season.ymir.common.utils;

import org.season.ymir.common.entity.ServiceBean;

/**
 * 服务导出信息工具
 *
 * @author KevinClair
 **/
public class ExportServiceBeanUriUtils {

    /**
     * 上传至zookeeper节点的数据
     *
     * @param serviceBean 服务Bean实例
     * @return zNode节点的数据
     */
    public static String buildUri(ServiceBean serviceBean) {
        // TODO 上传至zookeeper的数据重写
        return String.join("?", String.join("/", String.join("//", serviceBean.getProtocol()+":", serviceBean.getAddress()), serviceBean.getName()), appendProperty(serviceBean));
    }

    /**
     * 属性拼接
     *
     * @param serviceBean 服务Bean实例
     * @return 字符串
     */
    private static String appendProperty(ServiceBean serviceBean) {
        return String.join("&", "protocol=" + serviceBean.getProtocol(), "weight=" + serviceBean.getWeight());
    }
}
