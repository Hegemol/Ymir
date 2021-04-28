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

    public ServiceBeanEvent(String name, Class<?> clazz, Object bean, Integer weight, String path, String url) {
        super(name, clazz, bean, weight);
        this.path = path;
        this.url = url;
    }

    public ServiceBeanEvent(String name, Class<?> clazz, Object bean, Integer weight) {
        super(name, clazz, bean, weight);
    }

    public ServiceBeanEvent() {
    }
}
