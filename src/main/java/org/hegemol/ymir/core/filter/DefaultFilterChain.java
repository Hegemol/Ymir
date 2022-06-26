package org.hegemol.ymir.core.filter;

import org.hegemol.ymir.common.constant.CommonConstant;
import org.hegemol.ymir.common.model.InvocationMessage;
import org.hegemol.ymir.spi.loader.ExtensionLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认执行器处理器
 */
public class DefaultFilterChain implements FilterChain {

    private int index = 0;

    private final List<Filter> filterList;

    public DefaultFilterChain(final List<String> filterNames, final String side) {
        // 添加默认的执行器
        filterList = new ArrayList<>();
        if (side.equals(CommonConstant.SERVICE_PROVIDER_SIDE)){
            // provider端，默认添加ProviderRpcContextFilter
            filterNames.add(CommonConstant.PROVIDER_RPC_CONTEXT_FILTER);
        } else {
            // consumer端，默认添加ConsumerRpcContextFilter
            filterNames.add(0, CommonConstant.CONSUMER_RPC_CONTEXT_FILTER);
        }
        filterList.addAll(ExtensionLoader.getExtensionLoader(Filter.class).getLoader(filterNames).values());
    }

    @Override
    public void execute(final InvocationMessage message) {
        if (index < filterList.size()){
            Filter filter = filterList.get(index++);
            filter.execute(this, message);
        }
    }
}
