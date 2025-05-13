package org.hegemol.ymir.server.discovery;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.hegemol.ymir.client.NettyChannelManager;
import org.hegemol.ymir.client.NettyClient;
import org.hegemol.ymir.common.constant.CommonConstant;
import org.hegemol.ymir.common.entity.ServiceBean;
import org.hegemol.ymir.common.exception.RpcException;
import org.hegemol.ymir.common.utils.GsonUtils;
import org.hegemol.ymir.common.utils.ZkPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * zk注册中心下服务发现
 *
 * @author KevinClair
 **/
public class ZookeeperServiceDiscovery extends DefaultAbstractServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperServiceDiscovery.class);

    private CuratorFramework zkClient;
    private NettyClient nettyClient;

    public ZookeeperServiceDiscovery(CuratorFramework zkClient, NettyClient nettyClient) {
        this.zkClient = zkClient;
        this.nettyClient = nettyClient;
    }

    @Override
    protected void handleClient(List<ServiceBean> serviceList) {
        for (ServiceBean each : serviceList) {
            boolean clientIsExisted = NettyChannelManager.contains(each.getAddress());
            if (!clientIsExisted){
                nettyClient.connect(each.getAddress());
            }
        }
    }

    @Override
    public List<ServiceBean> findServiceListByRegisterCenter(String name) throws Exception {
        String servicePath = CommonConstant.PATH_DELIMITER + name + CommonConstant.PATH_DELIMITER + CommonConstant.SERVICE_PROVIDER_SIDE;
        List<String> children = zkClient.getChildren().forPath(servicePath);
        if (CollectionUtils.isEmpty(children)){
            throw new RpcException(String.format("No provider available for service %s from registry address %s",name, zkClient.getZookeeperClient().getCurrentConnectionString()));
        }
        List<ServiceBean> serviceList = Optional.ofNullable(children).orElseGet(() -> new ArrayList<>()).stream().map(path -> {
            String serviceBeanUri = null;
            try {
                serviceBeanUri = new String(zkClient.getData().forPath(String.join(CommonConstant.PATH_DELIMITER, servicePath, path)), CommonConstant.UTF_8);
            } catch (Exception e) {
                logger.error("Service List get error, exception:{}", ExceptionUtils.getStackTrace(e));
            }
            return GsonUtils.getInstance().fromJson(serviceBeanUri, ServiceBean.class);
        }).collect(Collectors.toList());
        super.put(name, serviceList);
        return serviceList;
    }

    @Override
    public void listener(List<String> serviceList, String address) {
        serviceList.forEach(name -> {
            try {
                // 节点监听
                String servicePath = CommonConstant.PATH_DELIMITER + name + CommonConstant.PATH_DELIMITER + CommonConstant.SERVICE_PROVIDER_SIDE;
                final PathChildrenCache childrenCache = new PathChildrenCache(zkClient, servicePath, true);
                childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
                childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(final CuratorFramework curatorFramework, final PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
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
                                List list = CollectionUtils.isEmpty(ZookeeperServiceDiscovery.super.get(serviceBean.getName())) ? new ArrayList() : ZookeeperServiceDiscovery.super.get(serviceBean.getName());
                                list.add(serviceBean);
                                ZookeeperServiceDiscovery.super.put(serviceBean.getName(), list);
                                break;
                            case CHILD_REMOVED:
                                ZookeeperServiceDiscovery.super.remove(serviceBean.getName());
                                break;
                            default:
                                break;
                        }
                    }
                });

                String consumerNode = CommonConstant.PATH_DELIMITER + name + CommonConstant.PATH_DELIMITER + CommonConstant.SERVICE_CONSUMER_SIDE;
                String registerZNodePath = ZkPathUtils.buildUriPath(consumerNode, address);
                // 写入consumer节点
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(registerZNodePath);
            } catch (Exception e) {
                logger.error("Zookeeper node add listener error, message:{}", ExceptionUtils.getStackTrace(e));
                throw new RpcException(e);
            }
        });
    }
}
