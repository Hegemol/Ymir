package org.season.ymir.core.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.season.ymir.core.discovery.YmirServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

//    @Override
//    public void eventReceived(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
//        logger.debug("Zookeeper node change, path:{}, childList:{}, node data:{}", curatorEvent.getPath(), curatorEvent.getChildren(), new String(curatorEvent.getData()));
//        // 只要子节点有改动就清空缓存
//        serviceDiscovery.remove(curatorEvent.getPath());
//    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
        // TODO 根据不同的节点变更类型，更新本地缓存和远程zookeeper节点数据
    }
}
