package org.season.ymir.core.filter;

import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.common.model.Response;

/**
 * 过滤器执行器
 *
 * @author KevinClair
 **/
public interface FilterChain {

    /**
     * 执行器
     *
     * @param bean
     * @return
     */
    Response execute(ServiceBean bean);
}
