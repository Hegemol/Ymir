package org.season.ymir.common.entity;

/**
 * 服务导出事件模型
 *
 * @author KevinClair
 */
public class ServiceBeanEvent extends ServiceBean {

    /**
     * 路径
     */
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
