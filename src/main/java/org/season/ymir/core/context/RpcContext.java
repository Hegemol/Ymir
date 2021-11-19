package org.season.ymir.core.context;

import java.util.HashMap;
import java.util.Map;

/**
 * Rpc上下文
 */
public class RpcContext {
    // attachments，存储元素值
    protected final Map<String, Object> attachments = new HashMap<>();

    public RpcContext() {
    }
}
