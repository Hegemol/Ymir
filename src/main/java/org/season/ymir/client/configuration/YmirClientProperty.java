package org.season.ymir.client.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TODO
 *
 * @author KevinClair
 */
@ConfigurationProperties(prefix = "ymir")
public class YmirClientProperty {

    private String port;

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
