package org.season.ymir.client;

import org.season.ymir.client.handler.NettyClientHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端缓存服务端管理器
 *
 * @author KevinClair
 **/
public class ClientCacheManager {


    /**
     * 已连接的服务缓存
     * key: 服务地址，格式：ip:port
     */
    private static final Map<String, NettyClientHandler> connectedServerNodes = new ConcurrentHashMap<>();

    /**
     * 获取缓存，根据远程地址获取客户端的处理器
     *
     * @param address 远程服务端地址
     * @return {@link NettyClientHandler}
     */
    public static NettyClientHandler get(String address){
        return connectedServerNodes.get(address);
    }

    /**
     * 设置缓存
     *
     * @param address 远程地址
     * @param handler 处理器
     */
    public static void put(String address, NettyClientHandler handler){
        connectedServerNodes.put(address, handler);
    }

    /**
     * 判断是否包含某个key
     *
     * @param address 服务端地址
     * @return 是否
     */
    public static boolean contains(String address){
        return connectedServerNodes.containsKey(address);
    }

    /**
     * 移除服务端
     *
     * @param address 服务端地址
     */
    public static void remove(String address){
        connectedServerNodes.remove(address);
    }
}
