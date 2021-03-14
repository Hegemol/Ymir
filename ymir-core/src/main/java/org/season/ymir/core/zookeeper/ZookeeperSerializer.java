package org.season.ymir.core.zookeeper;

import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import java.nio.charset.StandardCharsets;

/**
 * zookeeper序列化
 *
 * @author KevinClair
 */
public class ZookeeperSerializer implements ZkSerializer {

    @Override
    public byte[] serialize(Object o) throws ZkMarshallingError {
        return String.valueOf(o).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public Object deserialize(byte[] bytes) throws ZkMarshallingError {
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
