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
public class ConsumerRpcContextFilter implements Filter {

    @Override
    public void execute(final FilterChain filterChain, final InvocationMessage message) {
        // 将RpcContext的值设置到InvocationMessage的Headers中
        message.setHeaders(RpcContext.getContext().getAttachments());
        // 清空
        RpcContext.clear();
        filterChain.execute(message);
    }
}
