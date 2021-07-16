package org.season.ymir.core.protocol;


import org.season.ymir.common.model.YmirRequest;
import org.season.ymir.common.model.YmirResponse;
import org.season.ymir.common.utils.SerializingUtil;

/**
 * Protobuf序列化协议
 * @author 2YSP
 * @date 2020/8/5 21:22
 */
public class ProtoBufMessageProtocol implements MessageProtocol{

    @Override
    public byte[] marshalling(Object object) throws Exception {
        return SerializingUtil.serialize(object);
    }

    @Override
    public YmirRequest unmarshallingRequest(byte[] data) throws Exception {
        return SerializingUtil.deserialize(data,YmirRequest.class);
    }

    @Override
    public YmirResponse unmarshallingResponse(byte[] data) throws Exception {
        return SerializingUtil.deserialize(data,YmirResponse.class);
    }
}
