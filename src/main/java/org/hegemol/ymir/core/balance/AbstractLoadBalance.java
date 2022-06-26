package org.hegemol.ymir.core.balance;

import org.hegemol.ymir.common.entity.ServiceBean;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 抽象负载均衡器
 *
 * @author KevinClair
 **/
public abstract class AbstractLoadBalance implements LoadBalance{

    /**
     * 抽象方法
     *
     * @param services
     * @param address
     * @return
     */
    protected abstract ServiceBean loadMethod(List<ServiceBean> services, String address);

    @Override
    public ServiceBean load(List<ServiceBean> services, String address) {
        if (CollectionUtils.isEmpty(services)) {
            return null;
        }
        if (services.size() == 1) {
            return services.get(0);
        }
        return loadMethod(services, address);
    }
}
