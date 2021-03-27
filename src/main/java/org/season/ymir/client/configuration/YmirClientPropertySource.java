package org.season.ymir.client.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TODO
 *
 * @author KevinClair
 */
@ConfigurationProperties(prefix = "ymir")
public class YmirClientPropertySource {

    private String address;

    private String port;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
