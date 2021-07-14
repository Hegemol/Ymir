package org.season.ymir.common.constant;

/**
 * 常量
 *
 * @author KevinClair
 */
public class CommonConstant {

    /***
     * 编码
     */
    public static final String UTF_8 = "UTF-8";

    /**
     * Zookeeper服务注册地址
     */
    public static final String ZK_SERVICE_PATH = "/ymir";

    /**
     * Zookeeper客户端地址
     */
    public static final String ZK_SERVICE_PROVIDER_PATH = "providers";

    /**
     * Zookeeper服务端地址
     */
    public static final String ZK_SERVICE_SERVER_PATH = "consumers";

    /**
     * 路径分隔符
     */
    public static final String PATH_DELIMITER = "/";

    /**
     * 重连频率，单位：秒
     */
    public static final Integer RECONNECT_SECONDS = 20;

    /**
     * 心跳超时时间
     */
    public static final Integer READ_TIMEOUT_SECONDS = 60;

    /**
     * 心跳请求
     */
    public static final String HEART_BEAT_REQUEST = "HEART_BEAT_REQUEST";

    /**
     * 心跳响应
     */
    public static final String HEART_BEAT_RESPONSE = "HEART_BEAT_RESPONSE";
}
