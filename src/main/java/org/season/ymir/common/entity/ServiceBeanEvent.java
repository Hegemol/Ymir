package org.season.ymir.common.entity;

/**
 * 服务导出事件模型
 *
 * @author KevinClair
 */
public class ServiceBeanEvent extends ServiceBean {

    private String path;

    private String url;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ServiceBeanEvent(String name, Class<?> clazz, Object bean, Integer weight, String group, String version, String protocol, String path, String url) {
        super(name, clazz, bean, weight, group, version, protocol);
        this.path = path;
        this.url = url;
    }

    public ServiceBeanEvent(String name, Class<?> clazz, Object bean, String path, String url) {
        super(name, clazz, bean);
        this.path = path;
        this.url = url;
    }

    public ServiceBeanEvent(String path, String url) {
        this.path = path;
        this.url = url;
    }

    public ServiceBeanEvent() {
    }
}
