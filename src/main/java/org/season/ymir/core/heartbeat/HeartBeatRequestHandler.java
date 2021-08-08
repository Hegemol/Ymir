package org.season.ymir.core.heartbeat;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.season.ymir.common.base.InvocationType;
import org.season.ymir.common.model.InvocationMessage;
import org.season.ymir.common.model.YmirRequest;
import org.season.ymir.common.utils.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 心跳请求处理器
 *
 * @author KevinClair
 **/
@ChannelHandler.Sharable
public class HeartBeatRequestHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HeartBeatRequestHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        InvocationMessage<YmirRequest> request = (InvocationMessage<YmirRequest>) msg;
        if (request.getType().equals(InvocationType.HEART_BEAT_RQEUEST)){
            if (logger.isDebugEnabled()){
                logger.debug("Server heart beat request:{}", GsonUtils.getInstance().toJson(request));
            }
            InvocationMessage responseInvocationMessage = new InvocationMessage();
            responseInvocationMessage.setType(InvocationType.HEART_BEAT_RESPONSE);
            ctx.channel().writeAndFlush(responseInvocationMessage);
            ReferenceCountUtil.release(msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
