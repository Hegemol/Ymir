package org.season.ymir.core.filter;

import org.season.ymir.common.model.InvocationMessage;

import java.util.List;

/**
 * 默认执行器处理器
 */
public class DefaultFilterChain implements FilterChain {

    private int index = 0;

    private final List<Filter> filterList;

    public DefaultFilterChain(final List<Filter> filterList) {
        this.filterList = filterList;
    }

    @Override
    public void execute(final InvocationMessage message) {
        if (index < filterList.size()){
            Filter filter = filterList.get(index++);
            filter.execute(this, message);
        }
    }
}
