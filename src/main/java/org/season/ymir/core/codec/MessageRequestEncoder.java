package org.season.ymir.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.season.ymir.common.model.YmirRequest;
import org.season.ymir.common.utils.GsonUtils;
import org.season.ymir.core.protocol.MessageProtocol;
import org.season.ymir.spi.loader.ExtensionLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息请求编码器
 *
 * @author KevinClair
 **/
public class MessageRequestEncoder extends MessageToByteEncoder<YmirRequest> {

    private static final Logger logger = LoggerFactory.getLogger(MessageRequestEncoder.class);

    private String protocol;

    public MessageRequestEncoder(String protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, YmirRequest ymirRequest, ByteBuf byteBuf) throws Exception {
        MessageProtocol messageProtocol = ExtensionLoader.getExtensionLoader(MessageProtocol.class).getLoader(protocol);
        byte[] request = messageProtocol.marshallingRequest(ymirRequest);
        byteBuf.writeInt(request.length);
        byteBuf.writeBytes(request);
        if (logger.isDebugEnabled()){
            logger.debug("Channel {} encoder request message success, message content:{}", channelHandlerContext.channel().id(), GsonUtils.getInstance().toJson(ymirRequest));
        }
    }
}
