package org.season.ymir.core.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.season.ymir.common.constant.CommonConstant;
import org.season.ymir.common.utils.ExportServiceBeanUriUtils;
import org.season.ymir.server.discovery.YmirServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * zk节点监听
 *
 * @author KevinClair
 **/
public class ZookeeperNodeChangeListener implements PathChildrenCacheListener {

    private static Logger logger = LoggerFactory.getLogger(ZookeeperNodeChangeListener.class);

    private YmirServiceDiscovery serviceDiscovery;

    public ZookeeperNodeChangeListener(YmirServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @Override
    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
        // TODO 根据不同的节点变更类型，更新本地缓存和远程zookeeper节点数据
        if (Objects.isNull(pathChildrenCacheEvent.getData())){
            return;
        }
        logger.debug("Ymir service listener change,event type:{}, path:{}, data:{}",pathChildrenCacheEvent.getType().name(), pathChildrenCacheEvent.getData().getPath(), new String(pathChildrenCacheEvent.getData().getData(), CommonConstant.UTF_8));
        switch (pathChildrenCacheEvent.getType()){
            case CHILD_ADDED:
                // TODO 根据znode节点数据，更新本地缓存；
                String uri = new String(pathChildrenCacheEvent.getData().getData());
                List list = CollectionUtils.isEmpty(serviceDiscovery.get("org.season.ymir.common.TestService")) ? new ArrayList() : serviceDiscovery.get("org.season.ymir.example.server.TestServiceImpl");
                list.add(ExportServiceBeanUriUtils.getServiceBeanFromUri(uri));
                serviceDiscovery.put(pathChildrenCacheEvent.getData().getPath(), list);
                break;
            case CHILD_REMOVED:
                serviceDiscovery.remove(pathChildrenCacheEvent.getData().getPath());
                break;
            case CHILD_UPDATED:
                String updateUrl = new String(pathChildrenCacheEvent.getData().getData());
                List updateUrlList = CollectionUtils.isEmpty(serviceDiscovery.get("org.season.ymir.common.TestService")) ? new ArrayList() : serviceDiscovery.get("org.season.ymir.example.server.TestServiceImpl");
                updateUrlList.add(ExportServiceBeanUriUtils.getServiceBeanFromUri(updateUrl));
                serviceDiscovery.put(pathChildrenCacheEvent.getData().getPath(), updateUrlList);
                break;
            default:
                break;
        }
    }
}
