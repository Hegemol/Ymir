package org.season.ymir.core.filter.impl;

import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.common.model.Response;
import org.season.ymir.core.filter.Filter;
import org.season.ymir.core.filter.FilterChain;
import org.season.ymir.spi.loader.ExtensionLoader;

import java.util.List;

/**
 * Des
 */
public class DefaultFilterChain implements FilterChain {

    @Override
    public Response execute(ServiceBean bean) {
        // 获取所有的过滤器
        final List<String> filter = bean.getFilter();
        // 增加默认的过滤器
        filter.add("rpcContext");
        filter.stream().forEach(each -> ExtensionLoader.getExtensionLoader(Filter.class).getLoader(each).handler(this, bean));
        return null;
    }
}
