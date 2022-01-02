package org.season.ymir.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.season.ymir.common.base.SerializationTypeEnum;
import org.season.ymir.common.constant.CommonConstant;
import org.season.ymir.common.model.InvocationMessageWrap;
import org.season.ymir.common.utils.GsonUtils;
import org.season.ymir.core.protocol.MessageProtocol;
import org.season.ymir.spi.loader.ExtensionLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 消息编码器，按照协议类型写入数据
 *
 *   0     1     2     3     4     5     6     7     8     9     10     11    12    13    14
 *   +-----+-----+-----+-----+----—+-----+-----+-----+-----+------+-----+-----+-----+-----+
 *   |   magic   code        |      requestId        | type|serial|       full length     |
 *   +-----------------------+-----------------------+-----+------+-----------------------+
 *   |                                                                                    |
 *   |                                       body                                         |
 *   |                                                                                    |
 *   |                                                                                    |
 *   +------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   4B requestId（请求的Id）    1B type（消息类型）
 * 1B serial（序列化类型）    4B  full length（消息长度）
 * body（object类型数据）
 *
 * @author KevinClair
 */
public class MessageEncoder extends MessageToByteEncoder<InvocationMessageWrap> {

    private static final Logger logger = LoggerFactory.getLogger(MessageEncoder.class);
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, InvocationMessageWrap object, ByteBuf byteBuf) throws Exception {
        // 魔法值
        byteBuf.writeBytes(CommonConstant.MAGIC_NUMBER);
        // 请求Id
        byteBuf.writeInt(object.getRequestId());
        // 消息类型
        byteBuf.writeByte(object.getType().getCode());
        // 序列化类型
        SerializationTypeEnum serial = object.getSerial();
        byteBuf.writeByte(serial.getCode());
        // 消息长度
        MessageProtocol protocol = ExtensionLoader.getExtensionLoader(MessageProtocol.class).getLoader(serial.getName());
        byte[] body = protocol.serialize(object.getData());
        byteBuf.writeInt(body.length);

        // 写入最终的消息
        byteBuf.writeBytes(body);
        if (logger.isDebugEnabled()){
            logger.debug("Channel {} encoder message success, message content:{}", channelHandlerContext.channel().id(), GsonUtils.getInstance().toJson(object));
        }
    }
}
