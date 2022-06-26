package org.hegemol.ymir.core.filter;

import org.hegemol.ymir.common.model.InvocationMessage;

/**
 * 过滤器执行器
 *
 * @author KevinClair
 **/
public interface FilterChain {

    /**
     * 执行器
     *
     * @param message 消息体
     * @return
     */
    void execute(InvocationMessage message);
}
