package org.season.ymir.server.discovery;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.curator.framework.CuratorFramework;
import org.season.ymir.client.YmirNettyClient;
import org.season.ymir.common.constant.CommonConstant;
import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.common.exception.RpcException;
import org.season.ymir.common.utils.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * zk注册中心下服务发现
 *
 * @author KevinClair
 **/
public class ZookeeperYmirServiceDiscovery extends DefaultAbstractYmirServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperYmirServiceDiscovery.class);

    private CuratorFramework zkClient;
    private YmirNettyClient nettyClient;

    public ZookeeperYmirServiceDiscovery(CuratorFramework zkClient, YmirNettyClient nettyClient) {
        this.zkClient = zkClient;
        this.nettyClient = nettyClient;
    }

    @Override
    protected void handleClient(List<ServiceBean> serviceList) {
        for (ServiceBean each : serviceList) {
            boolean clientIsExisted = YmirNettyClient.connectedServerNodes.containsKey(each.getAddress());
            if (!clientIsExisted){
                nettyClient.initClient(each.getAddress());
            }
        }
    }

    @Override
    public List<ServiceBean> findServiceList(String name) throws Exception {
        List<ServiceBean> serviceList = super.findServiceList(name);
        if (!CollectionUtils.isEmpty(serviceList)) {
            return serviceList;
        }
        String servicePath = CommonConstant.PATH_DELIMITER + name + CommonConstant.PATH_DELIMITER + CommonConstant.ZK_SERVICE_PROVIDER_PATH;
        List<String> children = zkClient.getChildren().forPath(servicePath);
        if (CollectionUtils.isEmpty(children)){
            throw new RpcException(String.format("No provider available for service %s from registry address %s",name, zkClient.getZookeeperClient().getCurrentConnectionString()));
        }
        serviceList = Optional.ofNullable(children).orElseGet(() -> new ArrayList<>()).stream().map(path -> {
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
}
