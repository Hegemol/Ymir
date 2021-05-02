package org.season.ymir.client.register;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.season.ymir.common.constant.CommonConstant;
import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.common.entity.ServiceBeanCache;
import org.season.ymir.common.entity.ServiceBeanEvent;
import org.season.ymir.common.register.DefaultAbstractServiceRegister;
import org.season.ymir.common.utils.ExportServiceBeanUriUtils;
import org.season.ymir.common.utils.ZkPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * zookeeper服务注册
 *
 * @author KevinClair
 */
public class ZookeeperServiceRegister extends DefaultAbstractServiceRegister implements ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperServiceRegister.class);

    private CuratorFramework zkClient;
    private ApplicationEventPublisher applicationEventPublisher;

    public ZookeeperServiceRegister(CuratorFramework zkClient, Integer port, String protocol) {
        this.zkClient = zkClient;
        this.port = port;
        this.protocol = protocol;
    }

    @Override
    public ServiceBeanCache getBean(String name) throws Exception {
        // TODO 先从内部缓存中读取数据信息，如果缓存中没有，从zookeeper中读取数据信息
        return super.getBean(name);
    }

    @Override
    public void registerBean(ServiceBean serviceBean, int weight) throws Exception {
        super.registerBean(serviceBean, weight);
        ServiceBeanEvent exportEventModel = new ServiceBeanEvent();
        exportEventModel.setName(serviceBean.getName());
        exportEventModel.setProtocol(protocol);
        exportEventModel.setAddress(serviceBean.getAddress());
        exportEventModel.setWeight(weight);

        // 服务发布至注册中心
        exportService(serviceBean, exportEventModel);

        // 发布事件
        applicationEventPublisher.publishEvent(new org.season.ymir.client.event.ServiceBeanExportEvent(exportEventModel));
    }

    /**
     * 服务导出
     *
     * @param model            服务注册模型
     * @param exportEventModel 服务导出事件模型
     */
    private void exportService(final ServiceBean model, final ServiceBeanEvent exportEventModel) throws Exception {
        String serviceName = model.getName();
        String uri = ExportServiceBeanUriUtils.buildUri(model);
        String zNodePath = ZkPathUtils.buildPath(CommonConstant.ZK_SERVICE_PROVIDER_PATH, serviceName);
        if (Objects.isNull(zkClient.checkExists().forPath(zNodePath))) {
            // 创建节点
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zNodePath);
        }
        exportEventModel.setPath(zNodePath);
        String registerZNodePath = ZkPathUtils.buildUriPath(zNodePath, model.getAddress());
        // 创建一个临时节点，会话失效即被清理
        zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(registerZNodePath, uri.getBytes(StandardCharsets.UTF_8));
        exportEventModel.setUrl(uri);
        logger.info("Service export to zookeeper success, register url:{}", uri);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
