package org.season.ymir.client.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.season.ymir.client.ClientCacheManager;
import org.season.ymir.common.base.ServiceStatusEnum;
import org.season.ymir.common.constant.CommonConstant;
import org.season.ymir.common.exception.RpcException;
import org.season.ymir.common.exception.RpcTimeoutException;
import org.season.ymir.common.model.InvocationMessage;
import org.season.ymir.common.model.InvocationMessageWrap;
import org.season.ymir.common.model.Request;
import org.season.ymir.common.model.Response;
import org.season.ymir.common.utils.GsonUtils;
import org.season.ymir.core.filter.DefaultFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
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
public class NettyClientHandler extends SimpleChannelInboundHandler<InvocationMessageWrap<Response>> {

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
     * 存储request的返回信息，key为每次请求{@link Request}的requestId，value为{@link CompletableFuture<InvocationMessage< Response >> }
     */
    private static Map<Integer, CompletableFuture<InvocationMessage<Response>>> requestMap = new ConcurrentHashMap<>();

    public NettyClientHandler(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.channel = ctx.channel();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        ClientCacheManager.remove(remoteAddress);
        logger.error("Channel unregistered with remoteAddress:{}", remoteAddress);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, InvocationMessageWrap<Response> data) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Client reads message:{}", GsonUtils.getInstance().toJson(data));
        }
        CompletableFuture<InvocationMessage<Response>> responseFuture = requestMap.get(data.getRequestId());
        // 如果超时导致requestMap中没有保存值，此处会返回null的future，直接操作会导致NullPointException.
        if (Objects.isNull(responseFuture)) {
            return;
        }
        responseFuture.complete(data.getData());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception occurred:{}", ExceptionUtils.getStackTrace(cause));
        ClientCacheManager.remove(remoteAddress);
        ctx.close();
    }

    public Response sendRequest(InvocationMessageWrap<Request> request) {
        InvocationMessage<Response> response;
        CompletableFuture<InvocationMessage<Response>> completableFuture = new CompletableFuture<>();
        requestMap.put(request.getRequestId(), completableFuture);
        InvocationMessage<Request> data = request.getData();
        try {
            new DefaultFilterChain(new ArrayList<>(Arrays.asList(data.getHeaders().get(CommonConstant.FILTER_FROM_HEADERS).split(","))), CommonConstant.SERVICE_CONSUMER_SIDE).execute(data);
            channel.writeAndFlush(request);
            // 等待响应
            response = completableFuture.get(data.getTimeout(), TimeUnit.MILLISECONDS);
        } catch (TimeoutException exception) {
            Response timeoutExceptionResponse = new Response(ServiceStatusEnum.ERROR);
            timeoutExceptionResponse.setThrowable(new RpcTimeoutException(String.format("Invoke remote method %s timeout with %s ms", String.join("#", data.getBody().getServiceName(), data.getBody().getMethod()), data.getTimeout())));
            return timeoutExceptionResponse;
        } catch (Exception e) {
            Response exceptionResponse = new Response(ServiceStatusEnum.ERROR);
            exceptionResponse.setThrowable(new RpcException(e));
            return exceptionResponse;
        } finally {
            requestMap.remove(request.getRequestId());
        }
        return response.getBody();
    }
}
