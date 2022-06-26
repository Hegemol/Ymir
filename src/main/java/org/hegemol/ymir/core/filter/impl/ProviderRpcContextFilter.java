package org.hegemol.ymir.core.filter.impl;

import org.hegemol.ymir.common.model.InvocationMessage;
import org.hegemol.ymir.core.context.RpcContext;
import org.hegemol.ymir.core.filter.Filter;
import org.hegemol.ymir.core.filter.FilterChain;

/**
 * RpcContext处理器
 *
 * @author KevinClair
 **/
public class ProviderRpcContextFilter implements Filter {

    @Override
    public void execute(final FilterChain filterChain, final InvocationMessage message) {
        // 将InvocationMessage的Headers中值放入RpcContext
        message.getHeaders().forEach((k, v) -> RpcContext.getContext().setAttachments((String)k, (String)v));
        filterChain.execute(message);
    }
}
