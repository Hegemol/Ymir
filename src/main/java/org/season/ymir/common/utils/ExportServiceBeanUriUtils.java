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
        return String.join("?", String.join("/", String.join("//", serviceBean.getProtocol()+":", serviceBean.getAddress()), serviceBean.getClazz().getName()), appendProperty(serviceBean));
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

    /**
     * 获取对象实例
     *
     * @param uri node节点中的数据
     * @return {@link ServiceBean}
     */
    public static ServiceBean getServiceBeanFromUri(String uri){
        // TODO 将znode节点中的uri数据转换成对象
        return new ServiceBean();
    }
}
