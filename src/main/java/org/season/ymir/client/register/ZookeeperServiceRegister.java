package org.season.ymir.client.register;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.season.ymir.common.constant.CommonConstant;
import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.common.entity.ServiceBeanCache;
import org.season.ymir.common.entity.ServiceBeanEvent;
import org.season.ymir.common.exception.RpcException;
import org.season.ymir.common.register.DefaultAbstractServiceRegister;
import org.season.ymir.common.utils.GsonUtils;
import org.season.ymir.common.utils.ZkPathUtils;
import org.season.ymir.core.event.ServiceBeanExportEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    public ServiceBeanCache getBean(String name) throws RpcException {
        return Optional.ofNullable(super.getBean(name)).orElseGet(() -> {
            try {
                String zNodePath = ZkPathUtils.buildPath(CommonConstant.ZK_SERVICE_PROVIDER_PATH, name);
                List<String> childrenNodePath = zkClient.getChildren().forPath(zNodePath);
                // 不存在子节点，异常上报
                if (CollectionUtils.isEmpty(childrenNodePath)){
                    return null;
                }
                ServiceBean serviceBean = GsonUtils.getInstance().fromJson(new String(zkClient.getData().forPath(childrenNodePath.get(0)), CommonConstant.UTF_8), ServiceBean.class);
                super.registerBean(serviceBean);
                return super.getBean(serviceBean.getName());
            } catch (Exception e) {
                logger.error("Get service error, error:{}", ExceptionUtils.getStackTrace(e));
                return null;
            }
        });

    }

    @Override
    public void registerBean(ServiceBean serviceBean) throws RpcException {
        super.registerBean(serviceBean);
        ServiceBeanEvent exportEventModel = new ServiceBeanEvent();
        exportEventModel.setName(serviceBean.getName());
        exportEventModel.setProtocol(protocol);
        exportEventModel.setAddress(serviceBean.getAddress());
        exportEventModel.setWeight(serviceBean.getWeight());
        exportEventModel.setGroup(serviceBean.getGroup());
        exportEventModel.setVersion(serviceBean.getVersion());

        try {
            // 服务发布至注册中心
            exportService(serviceBean, exportEventModel);
        } catch (Exception e) {
            logger.error("Export service to register center error, error message:{}", ExceptionUtils.getStackTrace(e));
            throw new RpcException(String.format("Export service %s to register center error", serviceBean.getName()));
        }

        // 发布事件
        applicationEventPublisher.publishEvent(new ServiceBeanExportEvent(exportEventModel));
    }

    /**
     * 服务导出
     *
     * @param model            服务注册模型
     * @param exportEventModel 服务导出事件模型
     */
    private void exportService(final ServiceBean model, final ServiceBeanEvent exportEventModel) throws Exception {
        String serviceName = model.getName();
        String zNodePath = ZkPathUtils.buildPath(CommonConstant.ZK_SERVICE_PROVIDER_PATH, serviceName);
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
}
