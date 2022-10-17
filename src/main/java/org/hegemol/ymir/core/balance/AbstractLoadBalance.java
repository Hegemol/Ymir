package org.hegemol.ymir.core.balance;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.hegemol.ymir.common.entity.ServiceBean;
import org.springframework.util.CollectionUtils;

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

    protected long hash(final String key) {
        // md5 byte
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }
        md5.reset();
        md5.update(key.getBytes(StandardCharsets.UTF_8));
        byte[] digest = md5.digest();
        // hash code, Truncate to 32-bits
        long h = 0;
        for (int i = 0; i < 4; i++) {
            h <<= 8;
            h |= ((int) digest[i]) & 0xFF;
        }
        return h;
    }
}
