package org.season.ymir.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.season.ymir.common.constant.CommonConstant;
import org.season.ymir.core.codec.MessageDecoder;
import org.season.ymir.core.codec.MessageEncoder;
import org.season.ymir.core.heartbeat.HeartBeatRequestHandler;
import org.season.ymir.core.property.ConfigurationProperty;
import org.season.ymir.core.protocol.MessageProtocol;
import org.season.ymir.server.handler.NettyServerHandler;
import org.season.ymir.spi.loader.ExtensionLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.net.InetSocketAddress;

/**
 * Netty服务端
 *
 * @author KevinClair
 */
public class NettyServer implements DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    /**
     * 心跳超时时间
     */
    private static final Integer READ_TIMEOUT_SECONDS = 3 * 60;

    private Channel channel;
    private ConfigurationProperty property;
    private NettyServerHandler nettyServerHandler;
    // 配置服务器
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    public NettyServer(ConfigurationProperty property, NettyServerHandler nettyServerHandler) {
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
                                    /*Netty提供的日志打印Handler，可以展示发送接收出去的字节*/
//                                    .addLast(new LoggingHandler(LogLevel.INFO))
                                    /*剥离接收到的消息的长度字段，拿到实际的消息报文的字节数组*/
                                    //.addLast(new LengthFieldBasedFrameDecoder(65535,
                                    //                0, 4, 0,
                                    //                0))
                                    // 空闲检测
                                    .addLast(new IdleStateHandler(CommonConstant.TIMEOUT_SECONDS, 0, 0))
                                    // 解码器
                                    .addLast(new MessageDecoder(65535, 0, 4, 0, 0))
                                    // 编码器
                                    .addLast(new MessageEncoder(ExtensionLoader.getExtensionLoader(MessageProtocol.class).getLoader(property.getProtocol())))
                                    // 心跳处理器
                                    .addLast(new HeartBeatRequestHandler())
                                    // 服务端处理器
                                    .addLast(nettyServerHandler);
                        }
                    });

            // 启动服务
            ChannelFuture future = bootstrap.bind().sync();
            if (future.isSuccess()) {
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
