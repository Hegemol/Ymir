package org.season.ymir.core.protocol;

import org.season.ymir.common.model.YmirRequest;
import org.season.ymir.common.model.YmirResponse;
import org.season.ymir.common.utils.GsonUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Gson序列化
 *
 * @author KevinClair
 **/
public class GsonMessageProtocol implements MessageProtocol {

    @Override
    public byte[] marshalling(Object object) throws Exception {
        return GsonUtils.getInstance().toJson(object).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public YmirRequest unmarshallingRequest(byte[] data) throws Exception {
        return GsonUtils.getInstance().fromJson(new String(data, Charset.forName("utf-8")), YmirRequest.class);
    }

    @Override
    public YmirResponse unmarshallingResponse(byte[] data) throws Exception {
        return GsonUtils.getInstance().fromJson(new String(data, Charset.forName("utf-8")), YmirResponse.class);
    }
}
