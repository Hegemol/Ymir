package org.season.ymir.core.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.season.ymir.common.constant.CommonConstant;
import org.season.ymir.core.discovery.YmirServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * zk节点监听
 *
 * @author KevinClair
 **/
public class CuratorListenerImpl implements PathChildrenCacheListener {

    private static Logger logger = LoggerFactory.getLogger(CuratorListenerImpl.class);

    private YmirServiceDiscovery serviceDiscovery;

    public CuratorListenerImpl(YmirServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
        // TODO 根据不同的节点变更类型，更新本地缓存和远程zookeeper节点数据
        logger.debug("Ymir service listener change,event type:{}, path:{}, data:{}",pathChildrenCacheEvent.getType().name(), pathChildrenCacheEvent.getData().getPath(), new String(pathChildrenCacheEvent.getData().getData(), CommonConstant.UTF_8));
        switch (pathChildrenCacheEvent.getType()){
            case CHILD_ADDED:
                serviceDiscovery.put(pathChildrenCacheEvent.getData().getPath(), Collections.EMPTY_LIST);
                break;
            case CHILD_REMOVED:
                serviceDiscovery.remove(pathChildrenCacheEvent.getData().getPath());
                break;
            case CHILD_UPDATED:
                serviceDiscovery.put(pathChildrenCacheEvent.getData().getPath(), Collections.EMPTY_LIST);
                break;
            default:
                break;
        }
    }
}
