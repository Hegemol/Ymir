package org.season.ymir.core.filter;

import org.season.ymir.common.entity.ServiceBean;

/**
 * 过滤器
 *
 * @author KevinClair
 **/
//@SPI()
public interface Filter {

    /**
     * 处理器
     *
     * @param filterChain 执行器 {@link FilterChain}
     * @param serviceBean {@link ServiceBean}
     */
    void handler(FilterChain filterChain, ServiceBean serviceBean);
}
