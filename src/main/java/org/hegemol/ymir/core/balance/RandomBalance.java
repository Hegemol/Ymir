package org.hegemol.ymir.core.balance;

import java.util.List;
import java.util.Random;

import org.hegemol.ymir.common.entity.ServiceBean;

/**
 * 随机算法
 */
public class RandomBalance extends AbstractLoadBalance {

    private static final Random random = new Random();

    @Override
    public ServiceBean loadMethod(List<ServiceBean> services, String address) {
        int length = services.size();
        // 标识符，用于识别是否权重相同
        boolean sameWeight = true;
        // 记录每个实例的权重
        int[] weights = new int[length];
        int firstUpstreamWeight = getWeight(services.get(0));
        weights[0] = firstUpstreamWeight;
        // 记录总权重
        int totalWeight = firstUpstreamWeight;
        for (int i = 1; i < length; i++) {
            int currentUpstreamWeight = getWeight(services.get(i));
            weights[i] = currentUpstreamWeight;
            totalWeight += currentUpstreamWeight;
            if (sameWeight && currentUpstreamWeight != firstUpstreamWeight) {
                // 当前实例与第一个权重值不相同时
                sameWeight = false;
            }
        }
        if (totalWeight > 0 && !sameWeight) {
            return random(totalWeight, weights, services);
        }
        return random(services);
    }

    private int getWeight(ServiceBean serviceBean){
        return serviceBean.getWeight();
    }

    private ServiceBean random(final int totalWeight, final int[] weights, final List<ServiceBean> services) {
        // 总权重中取一个随机数
        int offset = random.nextInt(totalWeight);
        // 计算当前的随机数在总权重的哪个位置
        for (int i = 0; i < weights.length; i++) {
            offset -= weights[i];
            if (offset < 0) {
                return services.get(i);
            }
        }
        return random(services);
    }

    private ServiceBean random(final List<ServiceBean> services) {
        return services.get(random.nextInt(services.size()));
    }
}
