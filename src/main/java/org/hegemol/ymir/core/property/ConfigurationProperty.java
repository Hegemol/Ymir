package org.hegemol.ymir.core.property;

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
    private String serial = "protostuff";

    /**
     * 扫描包路径
     */
    private String scanPackages;

    /**
     * Gets the value of port.
     *
     * @return the value of port
     */
    public Integer getPort() {
        return port;
    }

    /**
     * Sets the port.
     *
     * @param port port
     */
    public void setPort(final Integer port) {
        this.port = port;
    }

    /**
     * Gets the value of serial.
     *
     * @return the value of serial
     */
    public String getSerial() {
        return serial;
    }

    /**
     * Sets the serial.
     *
     * @param serial serial
     */
    public void setSerial(final String serial) {
        this.serial = serial;
    }

    /**
     * Gets the value of scanPackages.
     *
     * @return the value of scanPackages
     */
    public String getScanPackages() {
        return scanPackages;
    }

    /**
     * Sets the scanPackages.
     *
     * @param scanPackages scanPackages
     */
    public void setScanPackages(final String scanPackages) {
        this.scanPackages = scanPackages;
    }
}
