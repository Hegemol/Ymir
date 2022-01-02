package org.season.ymir.core.heartbeat;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.season.ymir.common.base.MessageTypeEnum;
import org.season.ymir.common.model.InvocationMessageWrap;
import org.season.ymir.common.model.Response;
import org.season.ymir.common.utils.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 心跳响应处理器
 *
 * @author KevinClair
 **/
@ChannelHandler.Sharable
public class HeartBeatResponseHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HeartBeatResponseHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 心跳回应
        InvocationMessageWrap invocationMessage = (InvocationMessageWrap<Response>) msg;
        if (invocationMessage.getType().equals(MessageTypeEnum.HEART_BEAT_RESPONSE)){
            if (logger.isDebugEnabled()){
                logger.debug("Client receive heart beat response:{}", GsonUtils.getInstance().toJson(msg));
            }
            ReferenceCountUtil.release(msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
