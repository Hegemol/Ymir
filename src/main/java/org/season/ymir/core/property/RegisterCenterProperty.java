package org.season.ymir.core.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * zookeeper连接信息
 *
 * @author KevinClair
 */
@ConfigurationProperties(prefix = "ymir.register")
public class RegisterCenterProperty {

    private String type = "zookeeper";

    private String url;

    private Integer sessionTimeout = 6000;

    private Integer connectionTimeout = 6000;

    private Integer retryTimes = 3;

    /**
     * Gets the value of type.
     *
     * @return the value of type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     *
     * @param type type
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * Gets the value of url.
     *
     * @return the value of url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the url.
     *
     * @param url url
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * Gets the value of sessionTimeout.
     *
     * @return the value of sessionTimeout
     */
    public Integer getSessionTimeout() {
        return sessionTimeout;
    }

    /**
     * Sets the sessionTimeout.
     *
     * @param sessionTimeout sessionTimeout
     */
    public void setSessionTimeout(final Integer sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    /**
     * Gets the value of connectionTimeout.
     *
     * @return the value of connectionTimeout
     */
    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Sets the connectionTimeout.
     *
     * @param connectionTimeout connectionTimeout
     */
    public void setConnectionTimeout(final Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Gets the value of retryTimes.
     *
     * @return the value of retryTimes
     */
    public Integer getRetryTimes() {
        return retryTimes;
    }

    /**
     * Sets the retryTimes.
     *
     * @param retryTimes retryTimes
     */
    public void setRetryTimes(final Integer retryTimes) {
        this.retryTimes = retryTimes;
    }
}
