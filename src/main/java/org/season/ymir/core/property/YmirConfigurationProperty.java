package org.season.ymir.core.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 配置信息
 *
 * @author KevinClair
 */
@ConfigurationProperties(prefix = "ymir")
public class YmirConfigurationProperty {

    /**
     * 服务端口
     */
    private Integer port = 20777;

    /**
     * 服务协议
     */
    private String protocol = "protoBuf";

    /**
     * 消息体的最大
     */
    private int maxSize = 1024;

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
}
