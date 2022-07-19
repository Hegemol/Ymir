package org.hegemol.ymir.client.register;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.hegemol.ymir.common.constant.CommonConstant;
import org.hegemol.ymir.common.entity.ServiceBean;
import org.hegemol.ymir.common.entity.ServiceBeanCache;
import org.hegemol.ymir.common.entity.ServiceBeanEvent;
import org.hegemol.ymir.common.exception.RpcException;
import org.hegemol.ymir.common.register.DefaultAbstractServiceRegister;
import org.hegemol.ymir.common.utils.GsonUtils;
import org.hegemol.ymir.common.utils.ZkPathUtils;
import org.hegemol.ymir.core.event.ServiceBeanExportEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * zookeeper服务注册
 *
 * @author KevinClair
 */
public class ZookeeperServiceRegister extends DefaultAbstractServiceRegister implements ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperServiceRegister.class);

    private final CuratorFramework zkClient;
    private ApplicationEventPublisher applicationEventPublisher;

    public ZookeeperServiceRegister(CuratorFramework zkClient, Integer port, String protocol) {
        this.zkClient = zkClient;
        this.port = port;
        this.protocol = protocol;
    }

    @Override
    protected ServiceBeanCache getIfNoCache(String name) throws RpcException {
        try {
            String zNodePath = ZkPathUtils.buildPath(CommonConstant.SERVICE_PROVIDER_SIDE, name);
            List<String> childrenNodePath = zkClient.getChildren().forPath(zNodePath);
            // 不存在子节点，异常上报
            if (CollectionUtils.isEmpty(childrenNodePath)) {
                return null;
            }
            ServiceBean serviceBean = GsonUtils.getInstance().fromJson(new String(zkClient.getData().forPath(childrenNodePath.get(0)), CommonConstant.UTF_8), ServiceBean.class);
            super.registerBean(serviceBean);
            return super.getBean(serviceBean.getName());
        } catch (Exception e) {
            logger.error("Get service error, error:{}", ExceptionUtils.getStackTrace(e));
            return null;
        }

    }

    @Override
    public void publishEvent(final ServiceBeanEvent serviceBeanEvent) {
        // 发布事件
        applicationEventPublisher.publishEvent(new ServiceBeanExportEvent(serviceBeanEvent));
    }

    /**
     * 服务导出
     *
     * @param model            服务注册模型
     * @param exportEventModel 服务导出事件模型
     */
    protected void exportService(final ServiceBean model, final ServiceBeanEvent exportEventModel) throws Exception {
        String serviceName = model.getName();
        String zNodePath = ZkPathUtils.buildPath(CommonConstant.SERVICE_PROVIDER_SIDE, serviceName);
        if (Objects.isNull(zkClient.checkExists().forPath(zNodePath))) {
            // 创建节点
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zNodePath);
        }
        exportEventModel.setPath(zNodePath);
        String registerZNodePath = ZkPathUtils.buildUriPath(zNodePath, model.getAddress());
        // 创建一个临时节点，会话失效即被清理，此处节点数据存储Json数据
        zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(registerZNodePath, GsonUtils.getInstance().toJson(model).getBytes(CommonConstant.UTF_8));
        logger.info("Service export to zookeeper success, register url:{}", GsonUtils.getInstance().toJson(model));
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    protected void unRegisterBean() throws Exception {
        // TODO: 2022/7/19 删除所有注册的节点
        zkClient.close();
    }
}
