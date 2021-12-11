package org.season.ymir.core.filter;

import org.season.ymir.common.model.InvocationMessage;
import org.season.ymir.spi.annodation.SPI;

/**
 * 过滤器
 *
 * @author KevinClair
 **/
@SPI("")
public interface Filter {

    /**
     * 处理器
     *
     * @param filterChain 执行器 {@link FilterChain}
     * @param message     message内容 {@link InvocationMessage}
     */
    void execute(FilterChain filterChain, InvocationMessage message);
}
