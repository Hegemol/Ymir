package org.season.ymir.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.season.ymir.common.utils.GsonUtils;
import org.season.ymir.core.protocol.MessageProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息相应编码器
 *
 * @author KevinClair
 **/
@ChannelHandler.Sharable
public class MessageEncoder extends MessageToByteEncoder<Object> {

    private static final Logger logger = LoggerFactory.getLogger(MessageEncoder.class);

    private MessageProtocol protocol;

    public MessageEncoder(MessageProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object object, ByteBuf byteBuf) throws Exception {
        byte[] request = protocol.marshalling(object);
        byteBuf.writeInt(request.length);
        byteBuf.writeBytes(request);
        if (logger.isDebugEnabled()){
            logger.debug("Channel {} encoder message success, message content:{}", channelHandlerContext.channel().id(), GsonUtils.getInstance().toJson(object));
        }
    }
}
