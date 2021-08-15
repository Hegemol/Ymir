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
import org.season.ymir.common.model.YmirFuture;
import org.season.ymir.common.model.YmirRequest;
import org.season.ymir.common.model.YmirResponse;
import org.season.ymir.common.utils.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
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

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    /**
     * 存储request的返回信息，key为每次请求{@link YmirRequest}的requestId，value为{@link YmirFuture<YmirResponse>}
     */
    private static Map<String, YmirFuture<InvocationMessage<YmirResponse>>> requestMap = new ConcurrentHashMap<>();

    public NettyClientHandler(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
        countDownLatch.countDown();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Connect to server successfully:{}", GsonUtils.getInstance().toJson(ctx));
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, InvocationMessage<YmirResponse> data) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Client reads message:{}", GsonUtils.getInstance().toJson(data));
        }
        YmirFuture<InvocationMessage<YmirResponse>> responseFuture = requestMap.get(data.getRequestId());
        // 如果超时导致requestMap中没有保存值，此处会返回null的future，直接操作会导致NullPointException.
        if (Objects.isNull(responseFuture)) {
            return;
        }
        responseFuture.setResponse(data);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception occurred:{}", ExceptionUtils.getStackTrace(cause));
        YmirClientCacheManager.remove(remoteAddress);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        YmirClientCacheManager.remove(remoteAddress);
        logger.error("Channel inactive with remoteAddress:{}", remoteAddress);
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
        YmirFuture<InvocationMessage<YmirResponse>> future = new YmirFuture<>();
        requestMap.put(request.getRequestId(), future);
        try {
            if (countDownLatch.await(request.getTimeout(), TimeUnit.MILLISECONDS)){
                channel.writeAndFlush(request);
                // 等待响应
                response = future.get(request.getTimeout(), TimeUnit.MILLISECONDS);
            } else {
                throw new TimeoutException();
            }
        } catch (TimeoutException exception) {
            YmirResponse timeoutExceptionResponse = new YmirResponse(ServiceStatusEnum.ERROR);
            timeoutExceptionResponse.setException(new RpcTimeoutException(String.format("Invoke remote method %s timeout with %s ms", String.join("#", request.getBody().getServiceName(), request.getBody().getMethod()), request.getTimeout())));
            return timeoutExceptionResponse;
        } catch (Exception e) {
            YmirResponse exceptionResponse = new YmirResponse(ServiceStatusEnum.ERROR);
            exceptionResponse.setException(new RpcException(e));
            return exceptionResponse;
        } finally {
            requestMap.remove(request.getRequestId());
        }
        return response.getBody();
    }
}
