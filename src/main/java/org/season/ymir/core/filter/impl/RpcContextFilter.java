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
public class RpcContextFilter implements Filter {

    @Override
    public void execute(final FilterChain filterChain, final InvocationMessage message) {
        // 将RpcContext的值设置到InvocationMessage的Headers中
        message.setHeaders(RpcContext.getContext().getAttachments());
        // 清空
        RpcContext.clear();
        filterChain.execute(message);
    }
}
