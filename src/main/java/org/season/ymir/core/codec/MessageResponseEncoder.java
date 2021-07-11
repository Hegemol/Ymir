package org.season.ymir.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.season.ymir.common.model.YmirResponse;

/**
 * 消息相应编码器
 *
 * @author KevinClair
 **/
public class MessageResponseEncoder extends MessageToByteEncoder<YmirResponse> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, YmirResponse ymirResponse, ByteBuf byteBuf) throws Exception {

    }
}
