package org.hegemol.ymir.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hegemol.ymir.client.handler.NettyClientHandler;
import org.hegemol.ymir.common.constant.CommonConstant;
import org.hegemol.ymir.core.codec.MessageDecoder;
import org.hegemol.ymir.core.codec.MessageEncoder;
import org.hegemol.ymir.core.heartbeat.HeartBeatClientHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Netty客户端
 *
 * @author KevinClair
 **/
public class NettyClient implements DisposableBean {

    private static Logger logger = LoggerFactory.getLogger(NettyClient.class);

    // TODO 可配置化
    private EventLoopGroup loopGroup = new NioEventLoopGroup(4);

    /**
     * 客户端初始化
     *
     * @param address 服务端地址
     */
    public void initClient(String address) {
        NettyClientHandler handler = new NettyClientHandler();
        this.startClient(address, handler);
    }

    private void startClient(String address, NettyClientHandler handler) {
        String[] addrInfo = address.split(":");
        final String serverAddress = addrInfo[0];
        final String serverPort = addrInfo[1];
        // 配置客户端
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(loopGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress(serverAddress, Integer.parseInt(serverPort))
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        ChannelPipeline pipeline = channel.pipeline();
                        pipeline
                                /*Netty提供的日志打印Handler，可以展示发送接收出去的字节*/
                                .addLast(new LoggingHandler(LogLevel.INFO))
                                // 空闲检测
                                .addLast(new IdleStateHandler(0, CommonConstant.WRITE_TIMEOUT_SECONDS, 0))
                                // 解码器
                                .addLast(new MessageDecoder(65535, 4, 4, -8, 0))
                                // 编码器
                                .addLast(new MessageEncoder())
                                // 心跳检测
                                .addLast(new HeartBeatClientHandler())
                                // 客户端业务处理器
                                .addLast(handler);
                    }
                });
        // 启用客户端连接
        try {
            bootstrap.connect().sync().addListener((ChannelFutureListener) channelFuture -> {
                if (!channelFuture.isSuccess()) {
                    this.reconnect(address, handler);
                    return;
                }
                NettyChannelManager.put(new InetSocketAddress(serverAddress, Integer.parseInt(serverPort)), channelFuture.channel());
                logger.info("Server address:{} connect successfully.", address);
            });
        } catch (InterruptedException e) {
            logger.error("Netty client start error:{}", ExceptionUtils.getStackTrace(e));
            this.close();
        }
    }

    /**
     * 重新链接服务端
     *
     * @param address 客户端地址
     * @param handler 处理器
     */
    public void reconnect(String address, NettyClientHandler handler) {
        loopGroup.schedule(() -> {
            if (logger.isDebugEnabled()) {
                logger.debug("Netty client start reconnect, address:{}", address);
            }
            this.startClient(address, handler);
        }, CommonConstant.RECONNECT_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void destroy() throws Exception {
        this.close();
    }

    /**
     * 关闭
     */
    private void close(){
        if (loopGroup != null){
            loopGroup.shutdownGracefully();
        }
    }
}
