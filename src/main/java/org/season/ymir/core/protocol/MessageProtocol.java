package org.season.ymir.core.protocol;

import org.season.ymir.common.model.YmirRequest;
import org.season.ymir.common.model.YmirResponse;
import org.season.ymir.spi.annodation.SPI;

/**
 * 消息协议
 *
 * @author KevinClair
 */
@SPI("java")
public interface MessageProtocol {
    /**
     * 编组请求
     * @param request 请求信息
     * @return 请求字节数组
     * @throws Exception
     */
    byte[] marshallingRequest(YmirRequest request) throws Exception;

    /**
     * 解组请求
     * @param data
     * @return
     * @throws Exception
     */
    YmirRequest unmarshallingRequest(byte[] data) throws Exception;

    /**
     * 编组响应
     * @param response
     * @return
     */
    byte[] marshallingResponse(YmirResponse response) throws Exception;

    /**
     * 解组响应
     * @param data
     * @return
     * @throws Exception
     */
    YmirResponse unmarshallingResponse(byte[] data) throws Exception;
}
