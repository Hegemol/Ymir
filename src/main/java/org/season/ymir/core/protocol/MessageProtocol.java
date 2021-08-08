package org.season.ymir.core.protocol;

import org.season.ymir.common.model.InvocationMessage;
import org.season.ymir.spi.annodation.SPI;

/**
 * 消息协议
 *
 * @author KevinClair
 */
@SPI("proto")
public interface MessageProtocol {
    /**
     * 编组请求
     * @param object 请求信息
     * @return 请求字节数组
     * @throws Exception
     */
    byte[] marshalling(Object object) throws Exception;

    /**
     * 解码
     *
     * @param data 需要被解码的参数
     * @return {@link InvocationMessage}
     * @throws Exception
     */
    InvocationMessage unmarshalling(byte[] data) throws Exception;
}
