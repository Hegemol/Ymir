package org.hegemol.ymir.core.balance;

import org.hegemol.ymir.common.entity.ServiceBean;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 轮询
 *
 * @author KevinClair
 **/
public class RoundRobinLoadBalance  extends AbstractLoadBalance{

    private final int recyclePeriod = 60000;

    private final ConcurrentMap<String, ConcurrentMap<String, WeightedRoundRobin>> methodWeightMap = new ConcurrentHashMap<>(16);

    private final AtomicBoolean updateLock = new AtomicBoolean();

    @Override
    public ServiceBean loadMethod(final List<ServiceBean> services, final String ip) {
        String key = services.get(0).getAddress();
        ConcurrentMap<String, WeightedRoundRobin> map = methodWeightMap.get(key);
        if (map == null) {
            methodWeightMap.putIfAbsent(key, new ConcurrentHashMap<>(16));
            map = methodWeightMap.get(key);
        }
        int totalWeight = 0;
        long maxCurrent = Long.MIN_VALUE;
        long now = System.currentTimeMillis();
        ServiceBean selectedInvoker = null;
        WeightedRoundRobin selectedWRR = null;
        for (ServiceBean service : services) {
            String rKey = service.getAddress();
            WeightedRoundRobin weightedRoundRobin = map.get(rKey);
            int weight = service.getWeight();
            if (weightedRoundRobin == null) {
                weightedRoundRobin = new WeightedRoundRobin();
                weightedRoundRobin.setWeight(weight);
                map.putIfAbsent(rKey, weightedRoundRobin);
            }
            if (weight != weightedRoundRobin.getWeight()) {
                //weight changed
                weightedRoundRobin.setWeight(weight);
            }
            long cur = weightedRoundRobin.increaseCurrent();
            weightedRoundRobin.setLastUpdate(now);
            if (cur > maxCurrent) {
                maxCurrent = cur;
                selectedInvoker = service;
                selectedWRR = weightedRoundRobin;
            }
            totalWeight += weight;
        }
        if (!updateLock.get() && services.size() != map.size() && updateLock.compareAndSet(false, true)) {
            try {
                // copy -> modify -> update reference
                ConcurrentMap<String, WeightedRoundRobin> newMap = new ConcurrentHashMap<>(map);
                newMap.entrySet().removeIf(item -> now - item.getValue().getLastUpdate() > recyclePeriod);
                methodWeightMap.put(key, newMap);
            } finally {
                updateLock.set(false);
            }
        }
        if (selectedInvoker != null) {
            selectedWRR.sel(totalWeight);
            return selectedInvoker;
        }
        // should not happen here
        return services.get(0);
    }

    /**
     * The type Weighted round robin.
     */
    protected static class WeightedRoundRobin {

        private int weight;

        private final AtomicLong current = new AtomicLong(0);

        private long lastUpdate;

        /**
         * Gets weight.
         *
         * @return the weight
         */
        int getWeight() {
            return weight;
        }

        /**
         * Sets weight.
         *
         * @param weight the weight
         */
        void setWeight(final int weight) {
            this.weight = weight;
            current.set(0);
        }

        /**
         * Increase current long.
         *
         * @return the long
         */
        long increaseCurrent() {
            return current.addAndGet(weight);
        }

        /**
         * Sel.
         *
         * @param total the total
         */
        void sel(final int total) {
            current.addAndGet(-1 * total);
        }

        /**
         * Gets last update.
         *
         * @return the last update
         */
        long getLastUpdate() {
            return lastUpdate;
        }

        /**
         * Sets last update.
         *
         * @param lastUpdate the last update
         */
        void setLastUpdate(final long lastUpdate) {
            this.lastUpdate = lastUpdate;
        }
    }
}
