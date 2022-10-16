package org.hegemol.ymir.core.balance;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.hegemol.ymir.common.entity.ServiceBean;

/**
 * 轮询
 *
 * @author KevinClair
 **/
public class RoundRobinLoadBalance  extends AbstractLoadBalance{

    private final int RECYCLE_PERIOD = 60000;

    private final ConcurrentMap<String, ConcurrentMap<String, WeightedRoundRobin>> methodWeightMap = new ConcurrentHashMap<>(16);

    @Override
    public ServiceBean loadMethod(final List<ServiceBean> services, final String ip) {
        String key = services.get(0).getAddress();
        ConcurrentMap<String, WeightedRoundRobin> map = methodWeightMap.computeIfAbsent(key, k -> new ConcurrentHashMap<String, WeightedRoundRobin>(16));
        int totalWeight = 0;
        long maxCurrent = Long.MIN_VALUE;
        long now = System.currentTimeMillis();
        ServiceBean selectedService = null;
        WeightedRoundRobin selectedWeightedRoundRobin = null;
        for (ServiceBean service : services) {
            String rKey = service.getAddress();
            int weight = service.getWeight();
            WeightedRoundRobin weightedRoundRobin = map.computeIfAbsent(rKey, k -> {
                WeightedRoundRobin roundRobin = new WeightedRoundRobin();
                roundRobin.setWeight(weight);
                return roundRobin;
            });
            if (weight != weightedRoundRobin.getWeight()) {
                //weight changed
                weightedRoundRobin.setWeight(weight);
            }
            long cur = weightedRoundRobin.increaseCurrent();
            weightedRoundRobin.setLastUpdate(now);
            if (cur > maxCurrent) {
                maxCurrent = cur;
                selectedService = service;
                selectedWeightedRoundRobin = weightedRoundRobin;
            }
            totalWeight += weight;
        }
        if (services.size() != map.size()){
            map.entrySet().removeIf(item -> now - item.getValue().getLastUpdate()> RECYCLE_PERIOD);
        }
        if (Objects.nonNull(selectedService)){
            selectedWeightedRoundRobin.setWeight(totalWeight);
            return selectedService;
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
