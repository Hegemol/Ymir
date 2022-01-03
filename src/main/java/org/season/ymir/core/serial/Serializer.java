package org.season.ymir.core.serial;

import org.season.ymir.common.model.InvocationMessage;
import org.season.ymir.spi.annodation.SPI;

/**
 * 消息协议
 *
 * @author KevinClair
 */
@SPI("protostuff")
public interface Serializer {

    /**
     * 编组请求
     *
     * @param object 请求信息
     * @return 请求字节数组
     * @throws Exception
     */
    byte[] serialize(Object object) throws Exception;

    /**
     * 解组
     *
     * @param data 需要被解码的参数
     * @return {@link InvocationMessage}
     * @throws Exception
     */
    InvocationMessage deserialize(byte[] data) throws Exception;
}
