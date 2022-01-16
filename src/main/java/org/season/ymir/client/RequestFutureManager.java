package org.season.ymir.client;

import org.season.ymir.common.model.InvocationMessage;
import org.season.ymir.common.model.InvocationMessageWrap;
import org.season.ymir.common.model.Request;
import org.season.ymir.common.model.Response;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 客户端请求工厂，存储请求id对应的请求返回信息
 *
 * @author KevinClair
 **/
public class RequestFutureManager {

    /**
     * 存储request的返回信息，key为每次请求{@link Request}的requestId，value为{@link CompletableFuture<InvocationMessage<  Response  >> }
     */
    private static Map<Integer, CompletableFuture<InvocationMessage<Response>>> requestMap = new ConcurrentHashMap<>();

    /**
     * 完成任务
     *
     * @param data 返回数据
     */
    public static void completeTask(InvocationMessageWrap<Response> data){
        CompletableFuture<InvocationMessage<Response>> responseFuture = requestMap.get(data.getRequestId());
        // 如果超时导致requestMap中没有保存值，此处会返回null的future，直接操作会导致NullPointException.
        if (Objects.isNull(responseFuture)) {
            return;
        }
        responseFuture.complete(data.getData());
    }

    /**
     * 添加任务
     *
     * @param requestId 请求id
     * @param future    future对象
     */
    public static void putTask(Integer requestId, CompletableFuture<InvocationMessage<Response>> future){
        requestMap.put(requestId, future);
    }

    /**
     * 删除任务
     *
     * @param requestId 请求id
     */
    public static void remove(Integer requestId){
        requestMap.remove(requestId);
    }
}
