package org.season.ymir.client.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.season.ymir.client.NettyChannelManager;
import org.season.ymir.client.RequestFutureManager;
import org.season.ymir.common.model.InvocationMessageWrap;
import org.season.ymir.common.model.Response;
import org.season.ymir.common.utils.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * 客户端channel管理器
 *
 * @author KevinClair
 **/
@ChannelHandler.Sharable
public class NettyClientHandler extends SimpleChannelInboundHandler<InvocationMessageWrap<Response>> {

    private static Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) throws Exception {
        NettyChannelManager.put((InetSocketAddress) ctx.channel().remoteAddress(), ctx.channel());
        logger.info("New channel add, info:{}", ctx.channel());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        NettyChannelManager.remove((InetSocketAddress) ctx.channel().remoteAddress());
        ctx.close();
        logger.error("Channel unregistered with remoteAddress:{}", ctx.channel().remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, InvocationMessageWrap<Response> data) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Client reads message:{}", GsonUtils.getInstance().toJson(data));
        }
        RequestFutureManager.completeTask(data);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception occurred:{}", ExceptionUtils.getStackTrace(cause));
        NettyChannelManager.remove((InetSocketAddress) ctx.channel().remoteAddress());
        ctx.close();
    }
}
