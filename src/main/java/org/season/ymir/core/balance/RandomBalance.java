package org.season.ymir.core.balance;

import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.spi.annodation.SPI;

import java.util.List;
import java.util.Random;

/**
 * 随机算法
 */
@SPI("random")
public class RandomBalance implements LoadBalance{

    private static Random random = new Random();

    @Override
    public ServiceBean load(List<ServiceBean> services) {
        return services.get(random.nextInt(services.size()));
    }
}
