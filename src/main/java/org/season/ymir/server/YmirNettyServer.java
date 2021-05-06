package org.season.ymir.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.season.ymir.core.property.YmirConfigurationProperty;
import org.season.ymir.server.handler.NettyServerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Netty服务端
 *
 * @author KevinClair
 */
public class YmirNettyServer implements DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(YmirNettyServer.class);
    /**
     * 心跳超时时间
     */
    private static final Integer READ_TIMEOUT_SECONDS = 3 * 60;

    private Channel channel;
    private YmirConfigurationProperty property;
    private NettyServerHandler nettyServerHandler;
    // 配置服务器
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    public YmirNettyServer(YmirConfigurationProperty property, NettyServerHandler nettyServerHandler) {
        this.property = property;
        this.nettyServerHandler = nettyServerHandler;
    }

    public void start() {
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(property.getPort()))
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            // 获得 Channel 对应的 ChannelPipeline
                            ChannelPipeline channelPipeline = channel.pipeline();
                            // 添加一堆 NettyServerHandler 到 ChannelPipeline 中
                            channelPipeline
                                    // 空闲检测
                                    .addLast(new ReadTimeoutHandler(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS))
                                    // 服务端处理器
                                    .addLast(nettyServerHandler);
                        }
                    });

            // 启动服务
            ChannelFuture future = bootstrap.bind().sync();
            if (future.isSuccess()){
                logger.debug("Netty Server started successfully.");
                channel = future.channel();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("netty sever started failed,msg:{}", ExceptionUtils.getStackTrace(e));
        }
    }

    @Override
    public void destroy() {
        // 关闭 Netty Server
        if (channel != null) {
            channel.close();
        }
        // 优雅关闭两个 EventLoopGroup 对象
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
