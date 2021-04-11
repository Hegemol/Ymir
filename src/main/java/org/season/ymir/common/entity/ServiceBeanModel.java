package org.season.ymir.common.entity;

/**
 * 服务注册
 *
 * @author KevinClair
 */
public class ServiceBeanModel {

    /**
     * 服务名称
     */
    private String name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

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

    public ServiceBeanModel(String name, String protocol, String address, Integer weight) {
        this.name = name;
        this.protocol = protocol;
        this.address = address;
        this.weight = weight;
    }

    public ServiceBeanModel() {
    }
}
