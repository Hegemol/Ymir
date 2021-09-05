package org.season.ymir.core.protocol;


import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;
import org.season.ymir.common.model.InvocationMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息协议
 *
 * @author KevinClair
 */
public class ProtostuffMessageProtocol implements MessageProtocol{

    // Schema缓存
    private Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

    private Objenesis objenesis = new ObjenesisStd(true);

    @Override
    public byte[] serialize(Object object) throws Exception {
        return serializeExecute(object);
    }

    @Override
    public InvocationMessage deserialize(final byte[] data) throws Exception {
        return deserializeExecute(data,InvocationMessage.class);
    }

    private <T> Schema<T> getSchema(Class<T> cls) {
        // for thread-safe
        return (Schema<T>) cachedSchema.computeIfAbsent(cls, RuntimeSchema::createFrom);
    }

    /**
     * 将目标类序列化为byte数组
     *
     * @param source 需要序列的对象
     * @param <T>    泛型类型
     * @return byte[]
     */
    private <T> byte[] serializeExecute(T source) {
        Class<T> cls = (Class<T>) source.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(cls);
            return ProtostuffIOUtil.toByteArray(source, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
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
    private <T> T deserializeExecute(byte[] source, Class<T> clazz) {
        try {
            T message = objenesis.newInstance(clazz);
            Schema<T> schema = getSchema(clazz);
            ProtostuffIOUtil.mergeFrom(source, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
