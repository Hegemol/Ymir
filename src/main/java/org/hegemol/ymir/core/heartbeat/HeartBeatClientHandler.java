package org.hegemol.ymir.core.heartbeat;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.hegemol.ymir.client.NettyChannelManager;
import org.hegemol.ymir.common.base.MessageTypeEnum;
import org.hegemol.ymir.common.base.SerializationTypeEnum;
import org.hegemol.ymir.common.constant.CommonConstant;
import org.hegemol.ymir.common.model.ChannelInfo;
import org.hegemol.ymir.common.model.InvocationMessageWrap;
import org.hegemol.ymir.common.model.Response;
import org.hegemol.ymir.common.utils.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * 心跳响应处理器
 *
 * @author KevinClair
 **/
@ChannelHandler.Sharable
public class HeartBeatClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HeartBeatClientHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 心跳回应
        InvocationMessageWrap invocationMessage = (InvocationMessageWrap<Response>) msg;
        if (invocationMessage.getType().equals(MessageTypeEnum.HEART_BEAT_RESPONSE)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Client receive heartbeat response:{}", GsonUtils.getInstance().toJson(msg));
            }
            NettyChannelManager.get((InetSocketAddress) ctx.channel().remoteAddress()).setRetryTimes(0);
            ReferenceCountUtil.release(msg);
        }
        ctx.fireChannelRead(msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                // 超过最大重试次数，关闭连接
                ChannelInfo channelInfo = NettyChannelManager.get((InetSocketAddress) ctx.channel().remoteAddress());
                if (channelInfo.getRetryTimes() > CommonConstant.MAX_HEARTBEAT_TIMES) {
                    logger.warn("Heartbeat check, it's more than 3 times since the last heartbeat,channel {} has lost connection.", ctx.channel().id());
                    NettyChannelManager.remove((InetSocketAddress) ctx.channel().remoteAddress());
                    ctx.close();
                    return;
                }
                // 写超时处理
                channelInfo.setRetryTimes(channelInfo.getRetryTimes() + 1);
                InvocationMessageWrap heartBeatInvocationMessage = new InvocationMessageWrap();
                heartBeatInvocationMessage.setType(MessageTypeEnum.HEART_BEAT_RQEUEST);
                heartBeatInvocationMessage.setSerial(SerializationTypeEnum.PROTOSTUFF);
                heartBeatInvocationMessage.setRequestId(Integer.MIN_VALUE);
                heartBeatInvocationMessage.setData(null);
                channelInfo.getChannel().writeAndFlush(heartBeatInvocationMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
            return;
        }
        super.userEventTriggered(ctx, evt);
    }
}
