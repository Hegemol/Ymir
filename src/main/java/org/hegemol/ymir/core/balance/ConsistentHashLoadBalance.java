package org.hegemol.ymir.core.balance;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.hegemol.ymir.common.entity.ServiceBean;

/**
 * 一致性hash
 *
 * @author KevinClair
 **/
public class ConsistentHashLoadBalance extends AbstractLoadBalance{

    private static final String INSTANCE = "-INSTANCE-";

    @Override
    protected ServiceBean loadMethod(final List<ServiceBean> services, final String address) {
        TreeMap<Long, ServiceBean> treeMap = new TreeMap<>();
        services.forEach(each -> {
            for (int i = 0; i < each.getFictitiousInstance()-1; i++) {
                Long hashKey = hash(each.getAddress() + INSTANCE + i);
                treeMap.put(hashKey, each);
            }
        });
        long clientHash = hash(address);
        SortedMap<Long, ServiceBean> sortedMap = treeMap.tailMap(clientHash);
        if (!sortedMap.isEmpty()) {
            return sortedMap.get(sortedMap.firstKey());
        }
        return treeMap.firstEntry().getValue();
    }
}
