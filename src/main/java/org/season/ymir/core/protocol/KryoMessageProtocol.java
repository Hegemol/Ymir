package org.season.ymir.core.protocol;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.season.ymir.common.model.YmirRequest;
import org.season.ymir.common.model.YmirResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Kryo序列化
 *
 * @author KevinClair
 **/
public class KryoMessageProtocol implements MessageProtocol {

    private static final ThreadLocal<Kryo> kryoLocal = new ThreadLocal<Kryo>() {
        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            kryo.setReferences(false);
            kryo.register(YmirRequest.class);
            kryo.register(YmirResponse.class);
            Kryo.DefaultInstantiatorStrategy strategy = (Kryo.DefaultInstantiatorStrategy) kryo.getInstantiatorStrategy();
            strategy.setFallbackInstantiatorStrategy(new StdInstantiatorStrategy());
            return kryo;
        }
    };

    /**
     * 获得当前线程的 Kryo 实例
     *
     * @return 当前线程的 Kryo 实例
     */
    public static Kryo getInstance() {
        return kryoLocal.get();
    }

    @Override
    public byte[] marshallingRequest(YmirRequest request) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Output output = new Output(bout);
        Kryo kryo = getInstance();
        kryo.writeClassAndObject(output, request);
        byte[] bytes = output.toBytes();
        output.flush();
        return bytes;
    }

    @Override
    public YmirRequest unmarshallingRequest(byte[] data) throws Exception {
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        Input input = new Input(bin);
        Kryo kryo = getInstance();
        YmirRequest request = (YmirRequest) kryo.readClassAndObject(input);
        input.close();
        return request;
    }

    @Override
    public byte[] marshallingResponse(YmirResponse response) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Output output = new Output(bout);
        Kryo kryo = getInstance();
        kryo.writeClassAndObject(output, response);
        byte[] bytes = output.toBytes();
        output.flush();
        return bytes;
    }

    @Override
    public YmirResponse unmarshallingResponse(byte[] data) throws Exception {
        ByteArrayInputStream bin = new ByteArrayInputStream(data);
        Input input = new Input(bin);
        Kryo kryo = getInstance();
        YmirResponse response = (YmirResponse) kryo.readClassAndObject(input);
        input.close();
        return response;
    }
}
