package org.season.ymir.core.heartbeat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.season.ymir.common.base.MessageTypeEnum;
import org.season.ymir.common.base.SerializationTypeEnum;
import org.season.ymir.common.model.InvocationMessageWrap;
import org.season.ymir.common.model.Request;
import org.season.ymir.common.utils.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 心跳请求处理器
 *
 * @author KevinClair
 **/
@ChannelHandler.Sharable
public class HeartBeatServerHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HeartBeatServerHandler.class);

    /**
     * Channel 映射，存储Channel信息
     */
    private ConcurrentMap<ChannelId, Channel> channels = new ConcurrentHashMap<>();

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        // 从管理器中添加
        channels.put(ctx.channel().id(), ctx.channel());
        logger.info("Netty server, one active channel add, channel info:{}", ctx.channel());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        // 移除 channels
        channels.remove(ctx.channel().id());
        logger.info("Netty server, one channel unregistered, channel info:{}", ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 移除 channels
        channels.remove(ctx.channel().id());
        // 断开连接
        ctx.close();
        logger.error("Netty server, one channel caught error, channel info:{}, exception:{}", ctx.channel(), ExceptionUtils.getStackTrace(cause));
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            // 判断是否为读事件
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE){
                Channel channel = ctx.channel();
                logger.warn("Heartbeat check, it's more than two minutes since the last heartbeat,channel {} has lost connection.", channel.id());
                channels.remove(channel.id());
                ctx.close();
            }
            return;
        }
        super.userEventTriggered(ctx, evt);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        InvocationMessageWrap<Request> request = (InvocationMessageWrap<Request>) msg;
        if (request.getType().equals(MessageTypeEnum.HEART_BEAT_RQEUEST)){
            if (logger.isDebugEnabled()) {
                logger.debug("Server heartbeat request:{}", GsonUtils.getInstance().toJson(request));
            }
            InvocationMessageWrap responseInvocationMessage = new InvocationMessageWrap();
            responseInvocationMessage.setType(MessageTypeEnum.HEART_BEAT_RESPONSE);
            responseInvocationMessage.setSerial(SerializationTypeEnum.PROTOSTUFF);
            responseInvocationMessage.setRequestId(Integer.MIN_VALUE);
            responseInvocationMessage.setData(null);
            ctx.channel().writeAndFlush(responseInvocationMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            ;
            ReferenceCountUtil.release(msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
