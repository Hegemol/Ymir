package org.season.ymir.core.filter.impl;

import org.season.ymir.common.model.InvocationMessage;
import org.season.ymir.core.context.RpcContext;
import org.season.ymir.core.filter.Filter;
import org.season.ymir.core.filter.FilterChain;

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
