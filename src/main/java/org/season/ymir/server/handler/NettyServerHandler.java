package org.season.ymir.server.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.season.ymir.common.model.InvocationMessageWrap;
import org.season.ymir.common.model.Request;
import org.season.ymir.common.model.Response;
import org.season.ymir.common.utils.GsonUtils;
import org.season.ymir.core.context.RpcContext;
import org.season.ymir.core.handler.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty服务端处理器
 *
 * @author KevinClair
 **/
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<InvocationMessageWrap<Request>> {

    private static final Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private final RequestHandler requestHandler;

    public NettyServerHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, InvocationMessageWrap<Request> msg) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Server receives message :{}", msg);
            }
            InvocationMessageWrap<Response> response = requestHandler.handleRequest(msg);
            ctx.writeAndFlush(response).addListener(future -> {
                if (future.isSuccess() && logger.isDebugEnabled()) {
                    logger.debug("Server return response:{}", GsonUtils.getInstance().toJson(response));
                }
            });
        } catch (Exception e) {
            logger.error("Server read exception:{}", ExceptionUtils.getStackTrace(e));
        } finally {
            RpcContext.clear();
        }
    }
}
