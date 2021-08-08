package org.season.ymir.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.season.ymir.common.model.InvocationMessage;
import org.season.ymir.common.model.YmirRequest;
import org.season.ymir.common.model.YmirResponse;
import org.season.ymir.common.utils.GsonUtils;
import org.season.ymir.common.utils.YmirThreadFactory;
import org.season.ymir.core.handler.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Netty服务端处理器
 *
 * @author KevinClair
 **/
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<InvocationMessage<YmirRequest>> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private RequestHandler requestHandler;
    private ExecutorService executorService;

    public NettyServerHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
        this.executorService = new ThreadPoolExecutor(4, 8,
                200, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new YmirThreadFactory("netty"));
    }

    /**
     * Channel 映射，存储Channel信息
     */
    private ConcurrentMap<ChannelId, Channel> channels = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
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
        ctx.channel().close();
        logger.error("Netty server, one channel caught error, channel info:{}, exception:{}", ctx.channel(), ExceptionUtils.getStackTrace(cause));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, InvocationMessage<YmirRequest> msg) throws Exception {
        executorService.submit(() -> {
            try {
                if (logger.isDebugEnabled()){
                    logger.debug("Server receives message :{}", msg);
                }
                InvocationMessage<YmirResponse> response = requestHandler.handleRequest(msg.getBody(), msg.getRequestId());
                if (logger.isDebugEnabled()){
                    logger.debug("Server return response:{}", GsonUtils.getInstance().toJson(response));
                }
                ctx.writeAndFlush(response);
            } catch (Exception e) {
                logger.error("Server read exception:{}", ExceptionUtils.getStackTrace(e));
            }
        });
    }
}
