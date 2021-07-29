package org.season.ymir.core.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.season.ymir.common.constant.CommonConstant;
import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.common.utils.GsonUtils;
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
        if (Objects.isNull(pathChildrenCacheEvent.getData())){
            return;
        }
        if (logger.isDebugEnabled()){
            logger.debug("Ymir service listener change,event type:{}, path:{}", pathChildrenCacheEvent.getType().name(), pathChildrenCacheEvent.getData().getPath());
        }
        String uri = new String(pathChildrenCacheEvent.getData().getData(), CommonConstant.UTF_8);
        ServiceBean serviceBean = GsonUtils.getInstance().fromJson(uri, ServiceBean.class);
        switch (pathChildrenCacheEvent.getType()){
            case CHILD_ADDED:
            case CHILD_UPDATED:
                List list = CollectionUtils.isEmpty(serviceDiscovery.get(serviceBean.getName())) ? new ArrayList() : serviceDiscovery.get(serviceBean.getName());
                list.add(serviceBean);
                serviceDiscovery.put(serviceBean.getName(), list);
                break;
            case CHILD_REMOVED:
                serviceDiscovery.remove(serviceBean.getName());
                break;
            default:
                break;
        }
    }
}
