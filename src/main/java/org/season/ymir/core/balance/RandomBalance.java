package org.season.ymir.core.balance;

import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.spi.annodation.SPI;

import java.util.List;
import java.util.Random;

/**
 * 随机算法
 */
@SPI("random")
public class RandomBalance extends AbstractLoadBalance{

    private static final Random random = new Random();

    public ServiceBean loadMethod(List<ServiceBean> services, String address){
        return services.get(random.nextInt(services.size()));
    }
}
