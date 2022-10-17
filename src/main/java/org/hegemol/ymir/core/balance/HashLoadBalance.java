package org.hegemol.ymir.core.balance;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.hegemol.ymir.common.entity.ServiceBean;

/**
 * hash
 *
 * @author KevinClair
 **/
public class HashLoadBalance extends AbstractLoadBalance{

    private static final String INSTANCE = "-INSTANCE";

    @Override
    protected ServiceBean loadMethod(final List<ServiceBean> services, final String address) {
        TreeMap<Long, ServiceBean> treeMap = new TreeMap<>();
        services.forEach(each -> {
            Long hashKey = hash(each.getAddress() + INSTANCE);
            treeMap.put(hashKey, each);
        });
        long clientHash = hash(address);
        SortedMap<Long, ServiceBean> sortedMap = treeMap.tailMap(clientHash);
        if (!sortedMap.isEmpty()) {
            return sortedMap.get(sortedMap.firstKey());
        }
        return treeMap.firstEntry().getValue();
    }
}
