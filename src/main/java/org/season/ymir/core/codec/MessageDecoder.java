package org.season.ymir.core.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.season.ymir.common.base.MessageTypeEnum;
import org.season.ymir.common.base.SerializationTypeEnum;
import org.season.ymir.common.constant.CommonConstant;
import org.season.ymir.common.model.InvocationMessage;
import org.season.ymir.common.model.InvocationMessageWrap;
import org.season.ymir.common.utils.GsonUtils;
import org.season.ymir.core.serial.Serializer;
import org.season.ymir.spi.loader.ExtensionLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * 自定义协议
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
public class MessageDecoder extends LengthFieldBasedFrameDecoder {

    private static final Logger logger = LoggerFactory.getLogger(MessageDecoder.class);

    public MessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decode = super.decode(ctx, in);
        if (decode instanceof ByteBuf){
            ByteBuf byteBuf = (ByteBuf) decode;
            // 判断可读长度
            if (byteBuf.readableBytes() >= CommonConstant.TOTAL_LENGTH){
                try {
                    return decode(byteBuf);
                } catch (Exception e) {
                    logger.error("Decode message error:{}", ExceptionUtils.getStackTrace(e));
                } finally {
                    // 释放
                    byteBuf.release();
                }
            }
        }
        return decode;
    }

    private Object decode(ByteBuf byteBuf) throws Exception {
        // 校验魔法值是否正确
        checkMagicNumber(byteBuf);
        // 读取requestId
        int requestId = byteBuf.readInt();
        // 读取消息类型
        byte type = byteBuf.readByte();
        // 序列化类型
        byte serialType = byteBuf.readByte();
        // 消息长度
        int messageLength = byteBuf.readInt();
        byte[] body = new byte[messageLength];
        byteBuf.readBytes(body);
        SerializationTypeEnum serializationType = SerializationTypeEnum.getType(serialType);
        InvocationMessage message = ExtensionLoader.getExtensionLoader(Serializer.class).getLoader(serializationType.getName()).deserialize(body);
        InvocationMessageWrap messageWrap = new InvocationMessageWrap();
        messageWrap.setRequestId(requestId);
        messageWrap.setType(MessageTypeEnum.getType(type));
        messageWrap.setSerial(serializationType);
        messageWrap.setData(message);

        if (logger.isDebugEnabled()){
            logger.debug("Channel {} decoder message success, message content:{}", GsonUtils.getInstance().toJson(message));
        }
        return messageWrap;
    }

    private void checkMagicNumber(ByteBuf byteBuf) {
        // 读取魔法值
        int magicNumberLength = CommonConstant.MAGIC_NUMBER.length;
        byte[] magicNums = new byte[magicNumberLength];
        byteBuf.readBytes(magicNums);
        for (int i = 0; i < magicNumberLength; i++) {
            if (magicNums[i] != CommonConstant.MAGIC_NUMBER[i]) {
                throw new IllegalArgumentException(String.format("Invalid magic code: %s", Arrays.toString(magicNums)));
            }
        }
    }
}
