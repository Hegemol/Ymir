package org.season.ymir.common.base;

/**
 * 请求类型
 *
 * @author KevinClair
 **/
public enum InvocationType {
    // 心跳请求
    HEART_BEAT_RQEUEST(),

    // 心跳响应
    HEART_BEAT_RESPONSE(),

    // 服务接口请求
    SERVICE_REQUEST(),

    // 服务接口响应
    SERVICE_RESPONSE();
}
