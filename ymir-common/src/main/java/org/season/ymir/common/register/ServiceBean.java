package org.season.ymir.common.register;

/**
 * 服务bean实例
 *
 * @author KevinClair
 */
public class ServiceBean {

    private String name;

    private Class<?> clazz;

    private Object bean;

    public ServiceBean(String name, Class<?> clazz, Object bean) {
        this.name = name;
        this.clazz = clazz;
        this.bean = bean;
    }

    public ServiceBean() {
    }

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
}
