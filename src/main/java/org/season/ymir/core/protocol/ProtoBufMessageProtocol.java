package org.season.ymir.core.protocol;


import org.season.ymir.common.model.YmirRequest;
import org.season.ymir.common.model.YmirResponse;
import org.season.ymir.common.utils.SerializingUtil;
import org.season.ymir.spi.annodation.SPI;

/**
 * Protobuf序列化协议
 * @author 2YSP
 * @date 2020/8/5 21:22
 */
@SPI("java")
public class ProtoBufMessageProtocol implements MessageProtocol{

    @Override
    public byte[] marshallingRequest(YmirRequest request) throws Exception {
        return SerializingUtil.serialize(request);
    }

    @Override
    public YmirRequest unmarshallingRequest(byte[] data) throws Exception {
        return SerializingUtil.deserialize(data,YmirRequest.class);
    }

    @Override
    public byte[] marshallingResponse(YmirResponse response) throws Exception {
        return SerializingUtil.serialize(response);
    }

    @Override
    public YmirResponse unmarshallingResponse(byte[] data) throws Exception {
        return SerializingUtil.deserialize(data,YmirResponse.class);
    }
}
