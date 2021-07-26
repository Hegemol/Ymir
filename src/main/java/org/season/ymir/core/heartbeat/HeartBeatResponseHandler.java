package org.season.ymir.core.heartbeat;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.season.ymir.common.constant.CommonConstant;
import org.season.ymir.common.model.YmirResponse;
import org.season.ymir.common.utils.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 心跳响应处理器
 *
 * @author KevinClair
 **/
@ChannelHandler.Sharable
public class HeartBeatResponseHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HeartBeatResponseHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 心跳回应
        YmirResponse response = (YmirResponse) msg;
        if (response.getRequestId().equals(CommonConstant.HEART_BEAT_RESPONSE)){
            if (logger.isDebugEnabled()){
                logger.debug("Client receive heart beat response:{}", GsonUtils.getInstance().toJson(msg));
            }
            ReferenceCountUtil.release(msg);
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
