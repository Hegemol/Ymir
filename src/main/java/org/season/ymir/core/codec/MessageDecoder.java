package org.season.ymir.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.season.ymir.common.model.InvocationMessage;
import org.season.ymir.common.utils.GsonUtils;
import org.season.ymir.core.protocol.MessageProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 消息解码器
 *
 * @author KevinClair
 **/
public class MessageDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(MessageDecoder.class);

    private MessageProtocol protocol;

    public MessageDecoder(MessageProtocol protocol) {
        this.protocol = protocol;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        // 标记当前读取位置
        byteBuf.markReaderIndex();
        // 判断是否能够读取length长度
        if (byteBuf.readableBytes() < 4){
            return;
        }
        // 读取长度
        int bytesSize = byteBuf.readInt();
        // 如果 message 不够可读，则退回到原读取位置
        if (byteBuf.readableBytes() < bytesSize) {
            byteBuf.resetReaderIndex();
            return;
        }
        byte[] bytes = new byte[bytesSize];
        byteBuf.readBytes(bytes);
        InvocationMessage decodeResponse = protocol.unmarshalling(bytes);
        list.add(decodeResponse);
        if (logger.isDebugEnabled()){
            logger.debug("Channel {} decoder message success, message content:{}", channelHandlerContext.channel().id(), GsonUtils.getInstance().toJson(decodeResponse));
        }
    }
}
