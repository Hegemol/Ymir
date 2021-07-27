package org.season.ymir.common.entity;

/**
 * 服务注册
 *
 * @author KevinClair
 */
public class ServiceBean {

    /**
     * 服务名，一般为服务的接口名
     */
    private String name;

    /**
     * 类对象名称
     */
    private String clazz;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public ServiceBean() {
    }

    public ServiceBean(String name, String clazz, String address, Integer weight, String group, String version) {
        this.name = name;
        this.clazz = clazz;
        this.address = address;
        this.weight = weight;
        this.group = group;
        this.version = version;
    }
}
