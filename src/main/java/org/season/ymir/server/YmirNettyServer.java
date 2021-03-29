package org.season.ymir.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ReferenceCountUtil;
import org.season.ymir.core.config.YmirConfigurationProperty;
import org.season.ymir.common.utils.YmirThreadFactory;
import org.season.ymir.core.handler.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Netty服务端
 *
 * @author KevinClair
 */
public class YmirNettyServer {

    private static final Logger logger = LoggerFactory.getLogger(YmirNettyServer.class);

    private Channel channel;
    private RequestHandler requestHandler;
    private ExecutorService executorService;
    private YmirConfigurationProperty property;

    public YmirNettyServer(YmirConfigurationProperty property, RequestHandler requestHandler) {
        this.property = property;
        this.requestHandler = requestHandler;
        this.executorService = new ThreadPoolExecutor(4, 8,
                200, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new YmirThreadFactory("netty"));
    }

    public void start() {
        // 配置服务器
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {

                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new ChannelRequestHandler());
                }
            });

            // 启动服务
            ChannelFuture future = b.bind(property.getPort()).sync();
            logger.debug("Server started successfully.");
            channel = future.channel();
            // 等待服务通道关闭
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("start netty sever failed,msg:{}", e.getMessage());
        } finally {
            // 释放线程组资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void stop() {
        this.channel.close();
    }

    private class ChannelRequestHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            logger.debug("Channel active :{}", ctx);
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
                    logger.error("server read exception", e);
                }
            });
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            // Close the connection when an exception is raised.
            cause.printStackTrace();
            logger.error("Exception occurred:{}", cause.getMessage());
            ctx.close();
        }
    }
}
