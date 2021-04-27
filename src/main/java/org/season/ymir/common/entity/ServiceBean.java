package org.season.ymir.common.entity;

/**
 * 服务注册
 *
 * @author KevinClair
 */
public class ServiceBean extends ServiceBeanCache {

    /**
     * 服务协议
     */
    private String protocol;
    /**
     *  服务地址，格式：ip:port
     */
    private String address;
    /**
     * 权重，越大优先级越高
     */
    private Integer weight;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    public ServiceBean(String name, Class<?> clazz, Object bean, Integer weight) {
        super(name, clazz, bean);
        this.protocol = "";
        this.address = "";
        this.weight = weight;
    }

    public ServiceBean(String name, Class<?> clazz, Object bean) {
        super(name, clazz, bean);
    }

    public ServiceBean() {
    }
}
