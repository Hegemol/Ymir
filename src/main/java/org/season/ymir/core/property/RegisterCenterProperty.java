package org.season.ymir.core.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Properties;

/**
 * zookeeper连接信息
 *
 * @author KevinClair
 */
@ConfigurationProperties(prefix = "ymir.register")
public class RegisterCenterProperty {

    private String type = "zookeeper";

    private String url;

    private Properties props = new Properties();

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
     * Gets the value of props.
     *
     * @return the value of props
     */
    public Properties getProps() {
        return props;
    }

    /**
     * Sets the props.
     *
     * @param props props
     */
    public void setProps(final Properties props) {
        this.props = props;
    }
}
