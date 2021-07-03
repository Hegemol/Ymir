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

    public ServiceBeanCache() {
    }

    public ServiceBeanCache(String name, Class<?> clazz, Object bean) {
        this.name = name;
        this.clazz = clazz;
        this.bean = bean;
    }
}
