package org.season.ymir.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.season.ymir.common.model.YmirResponse;
import org.season.ymir.common.utils.GsonUtils;
import org.season.ymir.core.protocol.MessageProtocol;
import org.season.ymir.spi.loader.ExtensionLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息相应编码器
 *
 * @author KevinClair
 **/
public class MessageResponseEncoder extends MessageToByteEncoder<YmirResponse> {

    private static final Logger logger = LoggerFactory.getLogger(MessageResponseEncoder.class);

    private String protocol;

    public MessageResponseEncoder(String protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, YmirResponse ymirResponse, ByteBuf byteBuf) throws Exception {
        MessageProtocol messageProtocol = ExtensionLoader.getExtensionLoader(MessageProtocol.class).getLoader(protocol);
        byte[] request = messageProtocol.marshallingResponse(ymirResponse);
        byteBuf.writeInt(request.length);
        byteBuf.writeBytes(request);
        if (logger.isDebugEnabled()){
            logger.debug("Channel {} encoder response message success, message content:{}", channelHandlerContext.channel().id(), GsonUtils.getInstance().toJson(ymirResponse));
        }
    }
}
