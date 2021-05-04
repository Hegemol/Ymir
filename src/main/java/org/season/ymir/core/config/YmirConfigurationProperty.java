package org.season.ymir.core.config;

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
    private String protocol = "java";

    /**
     * 负载均衡
     */
    private String loadBalance = "random";

    /**
     * 权重
     */
    private Integer weight = 1;

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

    public String getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(String loadBalance) {
        this.loadBalance = loadBalance;
    }
}
