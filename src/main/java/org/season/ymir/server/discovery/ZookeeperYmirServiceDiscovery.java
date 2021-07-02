package org.season.ymir.server.discovery;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.curator.framework.CuratorFramework;
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

    public ZookeeperYmirServiceDiscovery(CuratorFramework zkClient) {
        this.zkClient = zkClient;
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
        serviceList = Optional.ofNullable(children).orElse(new ArrayList<>()).stream().map(path -> {
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
