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

    /**
     * 获取对象实例
     *
     * @param uri node节点中的数据
     * @return {@link ServiceBean}
     */
    public static ServiceBean getServiceBeanFromUri(String uri){
        // TODO 从uri中解析Bean数据信息
        String[] split = uri.split("//");
        ServiceBean serviceBean = new ServiceBean();
//        try {
//            serviceBean.setClazz(Class.forName("org.season.ymir.example.server.TestServiceImpl"));
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
        serviceBean.setName("org.season.ymir.common.TestService");
        serviceBean.setAddress("192.168.0.106:20777");
        serviceBean.setProtocol("java");
        serviceBean.setWeight(0);
//        serviceBean.setBean();
        return serviceBean;
    }
}
