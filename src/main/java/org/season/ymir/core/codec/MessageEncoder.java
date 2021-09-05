package org.season.ymir.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.season.ymir.common.utils.GsonUtils;
import org.season.ymir.core.protocol.MessageProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息相应编码器
 *
 *   0     1     2     3     4
 *   +-----+-----+-----+-----+
 *   |      full length      |
 *   +-----------------------+
 *   |                       |
 *   |        body           |
 *   |                       |
 *   |      ... ...          |
 *   +-----------------------+
 * 4B full length（消息长度，不是消息总长度） body（object类型数据）
 *
 * @author KevinClair
 **/
public class MessageEncoder extends MessageToByteEncoder<Object> {

    private static final Logger logger = LoggerFactory.getLogger(MessageEncoder.class);

    private MessageProtocol protocol;

    public MessageEncoder(final MessageProtocol protocol)    {
        this.protocol = protocol;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object object, ByteBuf byteBuf) throws Exception {
        byte[] request = protocol.serialize(object);
        byteBuf.writeInt(request.length);
        byteBuf.writeBytes(request);
        if (logger.isDebugEnabled()){
            logger.debug("Channel {} encoder message success, message content:{}", channelHandlerContext.channel().id(), GsonUtils.getInstance().toJson(object));
        }
    }
}
