package org.season.ymir.core.zookeeper;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.curator.framework.CuratorFramework;
import org.season.ymir.common.constant.CommonConstant;
import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.common.utils.GsonUtils;
import org.season.ymir.server.YmirServerDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * zk注册中心下服务发现
 *
 * @author KevinClair
 **/
public class ZookeeperYmirServerDiscovery implements YmirServerDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperYmirServerDiscovery.class);

    private CuratorFramework zkClient;

    public ZookeeperYmirServerDiscovery(CuratorFramework zkClient) {
        this.zkClient = zkClient;
    }

    @Override
    public List<ServiceBean> findServiceList(String name) throws Exception {
        String servicePath = CommonConstant.ZK_SERVICE_PATH + CommonConstant.PATH_DELIMITER + name + "/service";
        List<String> children = zkClient.getChildren().forPath(servicePath);
        return Optional.ofNullable(children).orElse(new ArrayList<>()).stream().map(str -> {
            String deCh = null;
            try {
                deCh = URLDecoder.decode(str, CommonConstant.UTF_8);
            } catch (UnsupportedEncodingException e) {
                logger.error("Service List get error, exception:{}", ExceptionUtils.getStackTrace(e));
            }
            return GsonUtils.getInstance().fromJson(deCh, ServiceBean.class);
        }).collect(Collectors.toList());
    }

    public CuratorFramework getZkClient() {
        return zkClient;
    }
}
