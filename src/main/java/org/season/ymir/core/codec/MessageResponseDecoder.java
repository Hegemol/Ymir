package org.season.ymir.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.season.ymir.common.model.YmirResponse;
import org.season.ymir.common.utils.GsonUtils;
import org.season.ymir.core.protocol.MessageProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Response解码器
 *
 * @author KevinClair
 **/
public class MessageResponseDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(MessageResponseDecoder.class);

    private MessageProtocol protocol;

    public MessageResponseDecoder(MessageProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        byte[] bytes = new byte[byteBuf.readInt()];
        byteBuf.readBytes(bytes);
        YmirResponse ymirResponse = protocol.unmarshallingResponse(bytes);
        list.add(ymirResponse);
        if (logger.isDebugEnabled()){
            logger.debug("Channel {} decoder message success, message content:{}", channelHandlerContext.channel().id(), GsonUtils.getInstance().toJson(ymirResponse));
        }
    }
}
