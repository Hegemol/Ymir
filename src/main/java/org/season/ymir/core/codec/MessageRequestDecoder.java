package org.season.ymir.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.TooLongFrameException;
import org.season.ymir.common.base.ServiceStatusEnum;
import org.season.ymir.common.constant.CommonConstant;
import org.season.ymir.common.model.YmirRequest;
import org.season.ymir.common.model.YmirResponse;
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
public class MessageRequestDecoder extends ByteToMessageDecoder {

    private static final Logger logger = LoggerFactory.getLogger(MessageRequestDecoder.class);

    private MessageProtocol protocol;

    private int maxSize;

    public MessageRequestDecoder(MessageProtocol protocol, int maxSize) {
        this.protocol = protocol;
        this.maxSize = maxSize;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        int bytesSize = byteBuf.readInt();
        if (bytesSize > maxSize){
            byteBuf.clear();
            throw new TooLongFrameException("Message's have been beyond the max size: "+maxSize);
        }
        byte[] bytes = new byte[bytesSize];
        byteBuf.readBytes(bytes);
        YmirRequest ymirRequest = protocol.unmarshallingRequest(bytes);
        if (ymirRequest.getRequestId().equals(CommonConstant.HEART_BEAT_REQUEST)){
            logger.info("Server heart beat request:{}", GsonUtils.getInstance().toJson(ymirRequest));
            YmirResponse response = new YmirResponse(ServiceStatusEnum.SUCCESS);
            response.setRequestId(CommonConstant.HEART_BEAT_RESPONSE);
            channelHandlerContext.channel().writeAndFlush(response);
            return;
        }
        list.add(ymirRequest);
        if (logger.isDebugEnabled()){
            logger.debug("Channel {} decoder message success, message content:{}", channelHandlerContext.channel().id(), GsonUtils.getInstance().toJson(ymirRequest));
        }
    }
}
