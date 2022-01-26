package org.season.ymir.core.context;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Rpc上下文
 */
public class RpcContext {

    /**
     * Local context.
     */
    private static final ThreadLocal<RpcContext> LOCAL = ThreadLocal.withInitial(() -> new RpcContext());

    /**
     * Local context.
     */
    private static final ThreadLocal<CompletableFuture<Object>> FUTURE_CONTEXT = ThreadLocal.withInitial(() -> new CompletableFuture());

    // attachments，存储元素值
    protected final Map<String, String> attachments = new HashMap<>();

    public RpcContext() {
    }

    /**
     * Clear ThreadLocal.
     */
    public static void clear(){
        LOCAL.remove();
    }

    /**
     * get context.
     *
     * @return {@link RpcContext}
     */
    public static RpcContext getContext() {
        return LOCAL.get();
    }

    /**
     * set future context.
     */
    public static void setFuture(CompletableFuture<Object> future) {
        FUTURE_CONTEXT.set(future);
    }

    /**
     * get future context.
     *
     * @return {@link RpcContext}
     */
    public static CompletableFuture<Object> getFuture() {
        return FUTURE_CONTEXT.get();
    }


    /**
     * set attachments.
     *
     * @param key   key of context.
     * @param value value of context.
     */
    public void setAttachments(String key, String value) {
        attachments.put(key, value);
    }

    /**
     * get attachments.
     *
     * @return attachments.
     */
    public Map<String, String> getAttachments(){
        return attachments;
    }
}
