package org.hegemol.ymir.common.constant;

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
     * 客户端地址
     */
    public static final String SERVICE_PROVIDER_SIDE = "providers";

    /**
     * 服务端地址
     */
    public static final String SERVICE_CONSUMER_SIDE = "consumers";

    /**
     * 路径分隔符
     */
    public static final String PATH_DELIMITER = "/";

    /**
     * nacos实例分隔符
     */
    public static final String INSTANCE_DELIMITER = ":";

    /**
     * 重连频率，单位：秒
     */
    public static final Integer RECONNECT_SECONDS = 20;

    /**
     * 读写超时时间
     */
    public static final Integer READ_TIMEOUT_SECONDS = 120;

    /**
     * 写超时时间
     */
    public static final Integer WRITE_TIMEOUT_SECONDS = 30;

    /**
     * 心跳请求
     */
    public static final String HEART_BEAT_REQUEST = "HEART_BEAT_REQUEST";

    /**
     * 心跳响应
     */
    public static final String HEART_BEAT_RESPONSE = "HEART_BEAT_RESPONSE";

    public static final String DOT = ".";

    public static final String E = "e";

    public static final String LEFT_ANGLE_BRACKETS = "{";

    public static final String RIGHT_ANGLE_BRACKETS = "}";

    /**
     * provider端rpcContext过滤器
     */
    public static final String PROVIDER_RPC_CONTEXT_FILTER = "providerRpcContext";

    /**
     * consumer端rpcContext过滤器
     */
    public static final String CONSUMER_RPC_CONTEXT_FILTER = "consumerRpcContext";

    /**
     * 过滤器key
     */
    public static final String FILTER_FROM_HEADERS = "filters";

    /**
     * 魔法书，定义在消息头
     */
    public static final byte[] MAGIC_NUMBER = {(byte) 'y', (byte) 'm', (byte) 'i', (byte) 'r'};

    /**
     * 消息体的长度
     */
    public static final byte TOTAL_LENGTH = 14;

    /**
     * 心跳最大重试次数
     */
    public static final int MAX_HEARTBEAT_TIMES = 3;

    /**
     * 注册类型zookeeper
     */
    public static final String REGISTRY_TYPE_ZOOKEEPER = "zookeeper";

    /**
     * 注册类型nacos
     */
    public static final String REGISTRY_TYPE_NACOS = "nacos";

    /**
     * 默认空间
     */
    public static final String DEFAULT_NAMESPACE = "ymir";

    public static final String CONTEXT_SEP = "/";
}
