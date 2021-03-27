package org.season.ymir.client.register;

import org.I0Itec.zkclient.ZkClient;
import org.season.ymir.client.event.ServiceBeanExportEvent;
import org.season.ymir.common.constant.CommonConstant;
import org.season.ymir.common.entity.ServiceBeanExportEventModel;
import org.season.ymir.common.entity.ServiceBeanRegisterModel;
import org.season.ymir.common.register.DefaultAbstractServiceRegister;
import org.season.ymir.common.register.ServiceBean;
import org.season.ymir.common.utils.GsonUtils;
import org.season.ymir.core.zookeeper.ZookeeperSerializer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;

/**
 * TODO
 *
 * @author KevinClair
 */
public class ZookeeperServiceRegister extends DefaultAbstractServiceRegister implements ApplicationEventPublisherAware {

    private ZkClient zkClient;
    private ApplicationEventPublisher applicationEventPublisher;

    public ZookeeperServiceRegister(ZkClient zkClient, Integer port, String protocol, Integer weight) {
        this.zkClient = zkClient;
        zkClient.setZkSerializer(new ZookeeperSerializer());
        this.port = port;
        this.protocol = protocol;
        this.weight = weight;
    }

    @Override
    public ServiceBean getBean(String name) throws Exception {
        // TODO 先从内部缓存中读取数据信息，读取不到从zk中读取
        return super.getBean(name);
    }

    @Override
    public void registerBean(ServiceBean bean) throws Exception {
        super.registerBean(bean);
        ServiceBeanRegisterModel registerModel = new ServiceBeanRegisterModel();
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
    private void exportService(final ServiceBeanRegisterModel model, final ServiceBeanExportEventModel exportEventModel) {
        String serviceName = model.getName();
        String uri = GsonUtils.getInstance().toJson(model);
        try {
            uri = URLEncoder.encode(uri, CommonConstant.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String zodePath = CommonConstant.ZK_SERVICE_PATH + CommonConstant.ZK_SERVICE_CLIENT_PATH + CommonConstant.PATH_DELIMITER + serviceName + "/service";
        if (!zkClient.exists(zodePath)) {
            // 创建节点
            zkClient.createPersistent(zodePath, true);
        }
        exportEventModel.setPath(zodePath);
        String uriPath = zodePath + CommonConstant.PATH_DELIMITER + uri;
        if (zkClient.exists(uriPath)) {
            // 删除之前的节点
            zkClient.delete(uriPath);
        }
        // 创建一个临时节点，会话失效即被清理
        zkClient.createEphemeral(uriPath);
        exportEventModel.setUrl(uriPath);
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
