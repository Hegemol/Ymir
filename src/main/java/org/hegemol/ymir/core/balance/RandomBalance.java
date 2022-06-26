package org.hegemol.ymir.core.balance;

import org.hegemol.ymir.common.entity.ServiceBean;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 随机算法
 */
public class RandomBalance extends AbstractLoadBalance {

    private static final Random random = new Random();

    @Override
    public ServiceBean loadMethod(List<ServiceBean> services, String address) {
        // 计算总权重
        int totalWeight = calculateTotalWeight(services);
        // 判断是否是相同的权重值
        boolean sameWeight = isAllUpStreamSameWeight(services);

        if (totalWeight > 0 && !sameWeight) {
            return load(totalWeight, services);
        }
        return services.get(random.nextInt(services.size()));
    }

    private boolean isAllUpStreamSameWeight(List<ServiceBean> services) {
        return new HashSet<>(services.stream().map(each -> each.getWeight()).collect(Collectors.toList())).size() == 1;
    }

    private int calculateTotalWeight(List<ServiceBean> services) {
        return services.stream().mapToInt(each -> each.getWeight()).sum();
    }

    private ServiceBean load(final int totalWeight, final List<ServiceBean> services) {
        // If the weights are not the same and the weights are greater than 0, then random by the total number of weights
        int offset = random.nextInt(totalWeight);
        // Determine which segment the random value falls on
        for (ServiceBean serviceBean : services) {
            offset -= serviceBean.getWeight();
            if (offset < 0) {
                return serviceBean;
            }
        }
        return services.get(0);
    }
}
