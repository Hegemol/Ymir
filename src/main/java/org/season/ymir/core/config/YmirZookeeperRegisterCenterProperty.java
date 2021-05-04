package org.season.ymir.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * zookeeper连接信息
 *
 * @author KevinClair
 */
@ConfigurationProperties(prefix = "ymir.zookeeper")
public class YmirZookeeperRegisterCenterProperty {

    private String url;

    private Integer sessionTimeout = 6000;

    private Integer connectionTimeout = 6000;

    private Integer retryTimes = 3;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(Integer sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes) {
        this.retryTimes = retryTimes;
    }
}
