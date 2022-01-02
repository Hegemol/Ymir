package org.season.ymir.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.season.ymir.common.model.InvocationMessageWrap;
import org.season.ymir.common.model.Request;
import org.season.ymir.common.model.Response;
import org.season.ymir.common.utils.GsonUtils;
import org.season.ymir.core.ThreadPoolFactory;
import org.season.ymir.core.context.RpcContext;
import org.season.ymir.core.handler.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Netty服务端处理器
 *
 * @author KevinClair
 **/
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<InvocationMessageWrap<Request>> {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private final RequestHandler requestHandler;

    public NettyServerHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

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
        // 断开连接
        ctx.close();
        logger.error("Netty server, one channel caught error, channel info:{}, exception:{}", ctx.channel(), ExceptionUtils.getStackTrace(cause));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, InvocationMessageWrap<Request> msg) {
        ThreadPoolFactory.execute(() -> {
            try {
                if (logger.isDebugEnabled()){
                    logger.debug("Server receives message :{}", msg);
                }
                InvocationMessageWrap<Response> response = requestHandler.handleRequest(msg);
                if (logger.isDebugEnabled()){
                    logger.debug("Server return response:{}", GsonUtils.getInstance().toJson(response));
                }
                ctx.writeAndFlush(response);
            } catch (Exception e) {
                logger.error("Server read exception:{}", ExceptionUtils.getStackTrace(e));
            } finally {
                RpcContext.clear();
            }
        });
    }
}
