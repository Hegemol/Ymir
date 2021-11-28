package org.season.ymir.core.context;

import java.util.HashMap;
import java.util.Map;

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
    private static final ThreadLocal<RpcContext> SERVER_LOCAL = ThreadLocal.withInitial(() -> new RpcContext());

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
    public static RpcContext getContext(){
        return LOCAL.get();
    }


    /**
     * set attachments.
     *
     * @param key   key of context.
     * @param value value of context.
     */
    public void setAttachments(String key, String value){
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
