package org.season.ymir.core.protocol;

import org.season.ymir.common.model.InvocationMessage;
import org.season.ymir.common.utils.GsonUtils;

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
    public InvocationMessage unmarshalling(final byte[] data) throws Exception {
        return GsonUtils.getInstance().fromJson(new String(data, StandardCharsets.UTF_8), InvocationMessage.class);
    }
}
