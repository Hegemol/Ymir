package org.season.ymir.core.property;

/**
 * 配置信息
 *
 * @author KevinClair
 */
public class YmirConfigurationProperty {

    /**
     * 服务端口
     */
    private Integer port = 20777;

    /**
     * 服务协议
     */
    private String protocol = "proto";

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
}
