package org.season.ymir.server.handle;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.season.ymir.common.utils.YmirThreadFactory;
import org.season.ymir.core.handler.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Netty服务端处理器
 *
 * @author KevinClair
 **/
@ChannelHandler.Sharable
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

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
     * Channel 映射
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
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        executorService.submit(() -> {
            try {
                logger.debug("the server receives message :{}", msg);
                ByteBuf byteBuf = (ByteBuf) msg;
                // 消息写入reqData
                byte[] reqData = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(reqData);
                // 手动回收
                ReferenceCountUtil.release(byteBuf);
                byte[] respData = requestHandler.handleRequest(reqData);
                ByteBuf respBuf = Unpooled.buffer(respData.length);
                respBuf.writeBytes(respData);
                logger.debug("Send response:{}", respBuf);
                ctx.writeAndFlush(respBuf);
            } catch (Exception e) {
                logger.error("server read exception:{}", ExceptionUtils.getStackTrace(e));
            }
        });
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
