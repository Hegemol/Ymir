package org.season.ymir.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.season.ymir.common.model.YmirRequest;

/**
 * 消息请求编码器
 *
 * @author KevinClair
 **/
public class MessageRequestEncoder extends MessageToByteEncoder<YmirRequest> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, YmirRequest ymirRequest, ByteBuf byteBuf) throws Exception {

    }
}
