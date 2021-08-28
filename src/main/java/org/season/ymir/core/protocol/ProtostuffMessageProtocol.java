package org.season.ymir.core.protocol;


import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.season.ymir.common.model.InvocationMessage;

/**
 * 消息协议
 *
 * @author KevinClair
 */
public class ProtostuffMessageProtocol implements MessageProtocol{

    @Override
    public byte[] marshalling(Object object) throws Exception {
        return serialize(object);
    }

    @Override
    public InvocationMessage unmarshalling(final byte[] data) throws Exception {
        return deserialize(data,InvocationMessage.class);
    }

    /**
     * 将目标类序列化为byte数组
     *
     * @param source 需要序列的对象
     * @param <T>    泛型类型
     * @return byte[]
     */
    private <T> byte[] serialize(T source) {
        Schema<T> schema = RuntimeSchema.getSchema((Class<T>) source.getClass());
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            return ProtostuffIOUtil.toByteArray(source, schema, buffer);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 将byte数组序列化为目标类
     *
     * @param source 序列化后的字节数组
     * @param clazz  需要被反序列化成的对象
     * @param <T>    泛型类型
     * @return 反序列化后的对象
     */
    private <T> T deserialize(byte[] source, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        T t = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(source, t, schema);
        return t;
    }
}
