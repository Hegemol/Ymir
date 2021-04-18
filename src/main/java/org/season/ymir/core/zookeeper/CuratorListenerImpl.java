package org.season.ymir.core.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.season.ymir.core.cache.YmirServerDiscoveryCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * zk节点监听
 *
 * @author KevinClair
 **/
public class CuratorListenerImpl implements CuratorListener {

    private static Logger logger = LoggerFactory.getLogger(CuratorListenerImpl.class);

    @Override
    public void eventReceived(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
        logger.debug("Zookeeper node change, path:{}, childList:{}, node data:{}", curatorEvent.getPath(), curatorEvent.getChildren(), new String(curatorEvent.getData()));
        // 只要子节点有改动就清空缓存
        YmirServerDiscoveryCache.remove(curatorEvent.getPath());
    }
}
