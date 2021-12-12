package org.season.ymir.common.entity;

/**
 * ServiceBean缓存对象
 *
 * @author KevinClair
 **/
public class ServiceBeanCache {
    /**
     * 服务名
     */
    private String name;

    /**
     * 类实例
     */
    private Class<?> clazz;

    /**
     * 对象
     */
    private Object bean;

    /**
     * 过滤器
     */
    private String filter;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(final String filter) {
        this.filter = filter;
    }

    public ServiceBeanCache() {
    }

    public ServiceBeanCache(final String name, final Class<?> clazz, final Object bean, final String filter) {
        this.name = name;
        this.clazz = clazz;
        this.bean = bean;
        this.filter = filter;
    }
}
