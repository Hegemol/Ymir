package org.season.ymir.core.property;

/**
 * 配置信息
 *
 * @author KevinClair
 */
public class ConfigurationProperty {

    /**
     * 服务端口
     */
    private Integer port = 20777;

    /**
     * 服务协议
     */
    private String protocol = "protostuff";

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
