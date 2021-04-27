package org.season.ymir.client.register;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.season.ymir.common.constant.CommonConstant;
import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.common.entity.ServiceBeanCache;
import org.season.ymir.common.entity.ServiceBeanExportEvent;
import org.season.ymir.common.register.DefaultAbstractServiceRegister;
import org.season.ymir.common.utils.GsonUtils;
import org.season.ymir.common.utils.ZkPathUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.io.UnsupportedEncodingException;
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
//        String host = InetAddress.getLocalHost().getHostAddress();
//        String address = host + ":" + port;
//        serviceBeanModel.setAddress(address);
//        serviceBeanModel.setProtocol(protocol);

        ServiceBeanExportEvent exportEventModel = new ServiceBeanExportEvent();
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
    private void exportService(final ServiceBean model, final ServiceBeanExportEvent exportEventModel) throws Exception {
        String serviceName = model.getName();
        String uri = GsonUtils.getInstance().toJson(model);
        try {
            uri = URLEncoder.encode(uri, CommonConstant.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String zodePath = ZkPathUtils.buildPath(CommonConstant.ZK_SERVICE_PROVIDER_PATH, serviceName);
        if (Objects.nonNull(zkClient.checkExists().forPath(zodePath))) {
            // 创建节点
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zodePath);
        }
        // TODO 服务导出至zk重构；
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
