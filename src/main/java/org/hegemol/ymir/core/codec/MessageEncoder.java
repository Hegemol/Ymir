package org.hegemol.ymir.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.hegemol.ymir.common.base.MessageTypeEnum;
import org.hegemol.ymir.common.base.SerializationTypeEnum;
import org.hegemol.ymir.common.constant.CommonConstant;
import org.hegemol.ymir.common.model.InvocationMessageWrap;
import org.hegemol.ymir.common.utils.GsonUtils;
import org.hegemol.ymir.core.serial.Serializer;
import org.hegemol.ymir.spi.loader.ExtensionLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * 消息编码器，按照协议类型写入数据
 *
 *   0     1     2     3     4     5     6     7     8     9     10     11    12    13    14
 *   +-----+-----+-----+-----+----—+-----+-----+-----+-----+------+-----+-----+-----+-----+
 *   |   magic   code        |      full length      | type|serial|       requestId       |
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
        // 标记当前的写位置
        byteBuf.markWriterIndex();
        // 预留空间写入长度
        byteBuf.writerIndex(byteBuf.writerIndex() + 4);
        // 消息类型
        byteBuf.writeByte(object.getType().getCode());
        // 序列化类型
        SerializationTypeEnum serial = object.getSerial();
        byteBuf.writeByte(serial.getCode());
        // 请求Id
        byteBuf.writeInt(object.getRequestId());
        int fullLength = CommonConstant.TOTAL_LENGTH;
        // 如果不是心跳类型的请求，计算Body长度
        byte[] body = null;
        if (!object.getType().equals(MessageTypeEnum.HEART_BEAT_RESPONSE) && !object.getType().equals(MessageTypeEnum.HEART_BEAT_RQEUEST)) {
            // 消息长度
            Serializer protocol = ExtensionLoader.getExtensionLoader(Serializer.class).getLoader(serial.getName());
            body = protocol.serialize(object.getData());
            // 计算总长度
            fullLength += body.length;
        }
        // 写入最终的消息
        if (Objects.nonNull(body)) {
            byteBuf.writeBytes(body);
        }
        // 计算写入长度的位置
        int writerIndex = byteBuf.writerIndex();
        byteBuf.resetWriterIndex();
        byteBuf.writeInt(fullLength);
        byteBuf.writerIndex(writerIndex);
        // 回到写节点
        if (logger.isDebugEnabled()) {
            logger.debug("Channel {} encoder message success, message content:{}", channelHandlerContext.channel().id(), GsonUtils.getInstance().toJson(object));
        }
    }
}
