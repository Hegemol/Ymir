package org.season.ymir.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.season.ymir.client.handler.NettyClientHandler;
import org.season.ymir.common.constant.CommonConstant;
import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.common.model.InvocationMessageWrap;
import org.season.ymir.common.model.Request;
import org.season.ymir.common.model.Response;
import org.season.ymir.core.codec.MessageDecoder;
import org.season.ymir.core.codec.MessageEncoder;
import org.season.ymir.core.heartbeat.HeartBeatResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Netty客户端
 *
 * @author KevinClair
 **/
public class NettyClient {

    private static Logger logger = LoggerFactory.getLogger(NettyClient.class);

    // TODO 可配置化
    private EventLoopGroup loopGroup = new NioEventLoopGroup(4);

    /**
     * 发送请求
     *
     * @param rpcRequest 请求参数
     * @param service    服务信息
     * @return {@link Response}
     */
    public Response sendRequest(InvocationMessageWrap<Request> rpcRequest, ServiceBean service) {

        String address = service.getAddress();
        synchronized (address) {
            if (ClientCacheManager.contains(address)) {
                NettyClientHandler handler = ClientCacheManager.get(address);
                return handler.sendRequest(rpcRequest);
            }
            final NettyClientHandler handler = new NettyClientHandler(address);
            // 异步建立客户端
            startClient(address, handler);
            return handler.sendRequest(rpcRequest);
        }
    }

    /**
     * 客户端初始化
     *
     * @param address 服务端地址
     */
    public void initClient(String address) {
        final NettyClientHandler handler = new NettyClientHandler(address);
        startClient(address, handler);
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
                                .addLast(new IdleStateHandler(0, CommonConstant.TIMEOUT_SECONDS, 0))
                                // 解码器
                                .addLast(new MessageDecoder(65535, 10, 4, 0, 0))
                                // 编码器
                                .addLast(new MessageEncoder())
                                // 心跳检测
                                .addLast(new HeartBeatResponseHandler())
                                // 客户端业务处理器
                                .addLast(handler);
                    }
                });
        // 启用客户端连接
        bootstrap.connect().addListener((ChannelFutureListener) channelFuture -> {
            if (!channelFuture.isSuccess()) {
                reconnect(address, handler);
                return;
            }
            logger.info("Server address:{} connect successfully.", address);
            ClientCacheManager.put(address, handler);
        });
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
            startClient(address, handler);
        }, CommonConstant.RECONNECT_SECONDS, TimeUnit.SECONDS);
    }

}
