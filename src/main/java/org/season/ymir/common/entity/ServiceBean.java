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

    /**
     * 分组
     */
    private String group;

    /**
     * 版本
     */
    private String version;

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

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public ServiceBean(String name, Class<?> clazz, Object bean, Integer weight, String group, String version, String protocol) {
        super(name, clazz, bean);
        this.address = "";
        this.protocol = protocol;
        this.weight = weight;
        this.group = group;
        this.version = version;
    }

    public ServiceBean(String name, Class<?> clazz, Object bean) {
        super(name, clazz, bean);
    }

    public ServiceBean() {
    }
}
