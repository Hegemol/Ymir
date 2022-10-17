package org.hegemol.ymir.common.entity;

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

    /**
     * 过滤器
     */
    private String filter;

    /**
     * 虚拟节点个数
     */
    private int fictitiousInstance;

    /**
     * Gets the value of name.
     *
     * @return the value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets the value of clazz.
     *
     * @return the value of clazz
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Sets the clazz.
     *
     * @param clazz clazz
     */
    public void setClazz(final String clazz) {
        this.clazz = clazz;
    }

    /**
     * Gets the value of protocol.
     *
     * @return the value of protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets the protocol.
     *
     * @param protocol protocol
     */
    public void setProtocol(final String protocol) {
        this.protocol = protocol;
    }

    /**
     * Gets the value of address.
     *
     * @return the value of address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address.
     *
     * @param address address
     */
    public void setAddress(final String address) {
        this.address = address;
    }

    /**
     * Gets the value of weight.
     *
     * @return the value of weight
     */
    public Integer getWeight() {
        return weight;
    }

    /**
     * Sets the weight.
     *
     * @param weight weight
     */
    public void setWeight(final Integer weight) {
        this.weight = weight;
    }

    /**
     * Gets the value of group.
     *
     * @return the value of group
     */
    public String getGroup() {
        return group;
    }

    /**
     * Sets the group.
     *
     * @param group group
     */
    public void setGroup(final String group) {
        this.group = group;
    }

    /**
     * Gets the value of version.
     *
     * @return the value of version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version.
     *
     * @param version version
     */
    public void setVersion(final String version) {
        this.version = version;
    }

    /**
     * Gets the value of filter.
     *
     * @return the value of filter
     */
    public String getFilter() {
        return filter;
    }

    /**
     * Sets the filter.
     *
     * @param filter filter
     */
    public void setFilter(final String filter) {
        this.filter = filter;
    }

    /**
     * Gets the value of fictitiousInstance.
     *
     * @return the value of fictitiousInstance
     */
    public int getFictitiousInstance() {
        return fictitiousInstance;
    }

    /**
     * Sets the fictitiousInstance.
     *
     * @param fictitiousInstance fictitiousInstance
     */
    public void setFictitiousInstance(final int fictitiousInstance) {
        this.fictitiousInstance = fictitiousInstance;
    }

    public ServiceBean() {
    }

    public ServiceBean(String name, String clazz, String address, Integer weight, String group, String version, String filter, int fictitiousInstance) {
        this.name = name;
        this.clazz = clazz;
        this.address = address;
        this.weight = weight;
        this.group = group;
        this.version = version;
        this.filter = filter;
        this.fictitiousInstance = fictitiousInstance;
    }
}
