package org.hegemol.ymir.client;

import io.netty.channel.Channel;
import org.hegemol.ymir.common.model.HeartBeat;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端缓存服务端管理器
 *
 * @author KevinClair
 **/
public class NettyChannelManager {


    /**
     * 已连接的服务缓存
     * key: 服务地址，格式：ip:port
     */
    private static final Map<InetSocketAddress, HeartBeat> manager = new ConcurrentHashMap<>();

    /**
     * 获取缓存，根据远程地址获取客户端的处理器
     *
     * @param address 远程服务端地址
     * @return {@link Channel}
     */
    public static Channel get(String address){
        String[] split = address.split(":");
        return manager.get(new InetSocketAddress(split[0], Integer.parseInt(split[1]))).getChannel();
    }

    /**
     * 获取缓存，根据远程地址获取客户端的处理器
     *
     * @param address 远程服务端地址
     * @return {@link HeartBeat}
     */
    public static HeartBeat get(InetSocketAddress address){
        return manager.get(address);
    }

    /**
     * 设置缓存
     *
     * @param address 远程地址
     * @param channel 通道
     */
    public static void put(InetSocketAddress address, Channel channel){
        manager.put(address, new HeartBeat(channel));
    }

    /**
     * 判断是否包含某个key
     *
     * @param address 服务端地址
     * @return 是否
     */
    public static boolean contains(String address){
        String[] split = address.split(":");
        return manager.containsKey(new InetSocketAddress(split[0], Integer.parseInt(split[1])));
    }

    /**
     * 移除服务端
     *
     * @param address 服务端地址
     */
    public static void remove(InetSocketAddress address){
        manager.remove(address);
    }
}
