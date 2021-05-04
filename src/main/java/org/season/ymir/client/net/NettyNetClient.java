package org.season.ymir.client.net;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.common.model.YmirRequest;
import org.season.ymir.common.model.YmirResponse;
import org.season.ymir.common.utils.YmirThreadFactory;
import org.season.ymir.core.handler.SendRequestHandler;
import org.season.ymir.core.protocol.MessageProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Netty客户端
 *
 * @author KevinClair
 **/
public class NettyNetClient {

    private static Logger logger = LoggerFactory.getLogger(NettyNetClient.class);

    private static ExecutorService threadPool = new ThreadPoolExecutor(4, 10, 200,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000), new YmirThreadFactory("netty-client"));

    private EventLoopGroup loopGroup = new NioEventLoopGroup(4);

    /**
     * 已连接的服务缓存
     * key: 服务地址，格式：ip:port
     */
    public static Map<String, SendRequestHandler> connectedServerNodes = new ConcurrentHashMap<>();

    public YmirResponse sendRequest(YmirRequest rpcRequest, ServiceBean service, MessageProtocol messageProtocol) {

        String address = service.getAddress();
        synchronized (address) {
            if (connectedServerNodes.containsKey(address)) {
                SendRequestHandler handler = connectedServerNodes.get(address);
                return handler.sendRequest(rpcRequest);
            }

            String[] addrInfo = address.split(":");
            final String serverAddress = addrInfo[0];
            final String serverPort = addrInfo[1];
            final SendRequestHandler handler = new SendRequestHandler(messageProtocol, address);
            threadPool.submit(() -> {
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
                                                // 空闲检测
                                                .addLast(new IdleStateHandler(60, 0, 0))
                                                .addLast(new ReadTimeoutHandler(3 * 60))
                                                .addLast(handler);
                                    }
                                });
                        // 启用客户端连接
                        ChannelFuture channelFuture = bootstrap.connect();
                        channelFuture.addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                                if (!channelFuture.isSuccess()) {
                                    // TODO 重新连接
                                    logger.error("Netty client connect error,address:{}", address);
                                    return;
                                }
                                connectedServerNodes.put(address, handler);
                            }
                        });
                    }
            );
            return handler.sendRequest(rpcRequest);
        }
    }
}
