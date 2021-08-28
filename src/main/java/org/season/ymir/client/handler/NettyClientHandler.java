package org.season.ymir.client.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.season.ymir.client.YmirClientCacheManager;
import org.season.ymir.common.base.InvocationType;
import org.season.ymir.common.base.ServiceStatusEnum;
import org.season.ymir.common.exception.RpcException;
import org.season.ymir.common.exception.RpcTimeoutException;
import org.season.ymir.common.model.InvocationMessage;
import org.season.ymir.common.model.YmirRequest;
import org.season.ymir.common.model.YmirResponse;
import org.season.ymir.common.utils.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 客户端客户端请求处理器
 *
 * @author KevinClair
 **/
@ChannelHandler.Sharable
public class NettyClientHandler extends SimpleChannelInboundHandler<InvocationMessage<YmirResponse>> {

    private static Logger logger = LoggerFactory.getLogger(NettyClientHandler.class);

    /**
     * 远程请求地址
     */
    private String remoteAddress;

    /**
     * 通道
     */
    private volatile Channel channel;

    /**
     * 存储request的返回信息，key为每次请求{@link YmirRequest}的requestId，value为{@link CompletableFuture<InvocationMessage<YmirResponse>> }
     */
    private static Map<String, CompletableFuture<InvocationMessage<YmirResponse>>> requestMap = new ConcurrentHashMap<>();

    public NettyClientHandler(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        YmirClientCacheManager.remove(remoteAddress);
        logger.error("Channel unregistered with remoteAddress:{}", remoteAddress);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, InvocationMessage<YmirResponse> data) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Client reads message:{}", GsonUtils.getInstance().toJson(data));
        }
        CompletableFuture<InvocationMessage<YmirResponse>> responseFuture = requestMap.get(data.getRequestId());
        // 如果超时导致requestMap中没有保存值，此处会返回null的future，直接操作会导致NullPointException.
        if (Objects.isNull(responseFuture)) {
            return;
        }
        responseFuture.complete(data);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception occurred:{}", ExceptionUtils.getStackTrace(cause));
        YmirClientCacheManager.remove(remoteAddress);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (logger.isDebugEnabled()) {
                logger.debug("Client send heart beat");
            }
            InvocationMessage heartBeatInvocationMessage = new InvocationMessage();
            heartBeatInvocationMessage.setType(InvocationType.HEART_BEAT_RQEUEST);
            ctx.writeAndFlush(heartBeatInvocationMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            return;
        }
        super.userEventTriggered(ctx, evt);
    }

    public YmirResponse sendRequest(InvocationMessage<YmirRequest> request) {
        InvocationMessage<YmirResponse> response;
        CompletableFuture<InvocationMessage<YmirResponse>> completableFuture = new CompletableFuture<>();
        requestMap.put(request.getRequestId(), completableFuture);
        try {
            channel.writeAndFlush(request);
            // 等待响应
            response = completableFuture.get(request.getTimeout(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException exception) {
            YmirResponse timeoutExceptionResponse = new YmirResponse(ServiceStatusEnum.ERROR);
            timeoutExceptionResponse.setThrowable(new RpcTimeoutException(String.format("Invoke remote method %s timeout with %s ms", String.join("#", request.getBody().getServiceName(), request.getBody().getMethod()), request.getTimeout())));
            return timeoutExceptionResponse;
        } catch (Exception e) {
            YmirResponse exceptionResponse = new YmirResponse(ServiceStatusEnum.ERROR);
            exceptionResponse.setThrowable(new RpcException(e));
            return exceptionResponse;
        } finally {
            requestMap.remove(request.getRequestId());
        }
        return response.getBody();
    }
}
