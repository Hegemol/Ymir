package org.season.ymir.core.heartbeat;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.season.ymir.client.NettyChannelManager;
import org.season.ymir.common.base.MessageTypeEnum;
import org.season.ymir.common.base.SerializationTypeEnum;
import org.season.ymir.common.model.HeartBeat;
import org.season.ymir.common.model.InvocationMessage;
import org.season.ymir.common.model.InvocationMessageWrap;
import org.season.ymir.common.model.Response;
import org.season.ymir.common.utils.GsonUtils;
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
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE){
                // 超过最大重试次数，关闭连接
                HeartBeat heartBeat = NettyChannelManager.get((InetSocketAddress) ctx.channel().remoteAddress());
                if (heartBeat.getRetryTimes() > 3){
                    logger.warn("Heartbeat check, it's more than 3 times since the last heartbeat,channel {} has lost connection.", ctx.channel().id());
                    NettyChannelManager.remove((InetSocketAddress) ctx.channel().remoteAddress());
                    ctx.close();
                    return;
                }
                // 写超时处理
                heartBeat.setRetryTimes(heartBeat.getRetryTimes()+1);
                InvocationMessageWrap heartBeatInvocationMessage = new InvocationMessageWrap();
                heartBeatInvocationMessage.setType(MessageTypeEnum.HEART_BEAT_RQEUEST);
                heartBeatInvocationMessage.setSerial(SerializationTypeEnum.PROTOSTUFF);
                heartBeatInvocationMessage.setData(new InvocationMessage());
                heartBeat.getChannel().writeAndFlush(heartBeatInvocationMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
            return;
        }
        super.userEventTriggered(ctx, evt);
    }
}
