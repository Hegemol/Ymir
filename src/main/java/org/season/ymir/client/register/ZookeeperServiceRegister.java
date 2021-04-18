package org.season.ymir.client.register;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.season.ymir.client.event.ServiceBeanExportEvent;
import org.season.ymir.common.constant.CommonConstant;
import org.season.ymir.common.entity.ServiceBeanExportEventModel;
import org.season.ymir.common.entity.ServiceBeanModel;
import org.season.ymir.common.register.DefaultAbstractServiceRegister;
import org.season.ymir.common.register.ServiceBean;
import org.season.ymir.common.utils.GsonUtils;
import org.season.ymir.common.utils.ZkPathUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.Objects;

/**
 * TODO
 *
 * @author KevinClair
 */
public class ZookeeperServiceRegister extends DefaultAbstractServiceRegister implements ApplicationEventPublisherAware {

    private CuratorFramework zkClient;
    private ApplicationEventPublisher applicationEventPublisher;

    public ZookeeperServiceRegister(CuratorFramework zkClient, Integer port, String protocol, Integer weight) {
        this.zkClient = zkClient;
        this.port = port;
        this.protocol = protocol;
        this.weight = weight;
    }

    @Override
    public ServiceBean getBean(String name) throws Exception {
        // TODO 先从内部缓存中读取数据信息
        return super.getBean(name);
    }

    @Override
    public void registerBean(ServiceBean bean) throws Exception {
        super.registerBean(bean);
        ServiceBeanModel registerModel = new ServiceBeanModel();
        String host = InetAddress.getLocalHost().getHostAddress();
        String address = host + ":" + port;
        registerModel.setAddress(address);
        registerModel.setName(bean.getName());
        registerModel.setProtocol(protocol);
        registerModel.setWeight(weight);

        ServiceBeanExportEventModel exportEventModel = new ServiceBeanExportEventModel();
        exportEventModel.setName(bean.getName());
        exportEventModel.setProtocol(protocol);
        exportEventModel.setAddress(address);
        exportEventModel.setWeight(weight);

        // 服务发布至注册中心
        exportService(registerModel, exportEventModel);

        // 发布事件
        applicationEventPublisher.publishEvent(new ServiceBeanExportEvent(exportEventModel));
    }

    /**
     * 服务导出
     *
     * @param model            服务注册模型
     * @param exportEventModel 服务导出事件模型
     */
    private void exportService(final ServiceBeanModel model, final ServiceBeanExportEventModel exportEventModel) throws Exception {
        String serviceName = model.getName();
        String uri = GsonUtils.getInstance().toJson(model);
        try {
            uri = URLEncoder.encode(uri, CommonConstant.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String zodePath = ZkPathUtils.buildPath(CommonConstant.ZK_SERVICE_CLIENT_PATH, serviceName);
        if (Objects.nonNull(zkClient.checkExists().forPath(zodePath))) {
            // 创建节点
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zodePath);
        }
        exportEventModel.setPath(zodePath);
        String uriPath = ZkPathUtils.buildUriPath(zodePath, uri);
        if (Objects.nonNull(zkClient.checkExists().forPath(uriPath))) {
            // 删除之前的节点
            zkClient.delete().guaranteed().forPath(uriPath);
        }
        // 创建一个临时节点，会话失效即被清理
        zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(uriPath);
        exportEventModel.setUrl(uriPath);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
