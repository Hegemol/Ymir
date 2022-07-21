package org.hegemol.ymir.client.register;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hegemol.ymir.common.constant.CommonConstant;
import org.hegemol.ymir.common.entity.ServiceBean;
import org.hegemol.ymir.common.entity.ServiceBeanCache;
import org.hegemol.ymir.common.entity.ServiceBeanEvent;
import org.hegemol.ymir.common.exception.RpcException;
import org.hegemol.ymir.common.register.DefaultAbstractServiceRegister;
import org.hegemol.ymir.common.utils.GsonUtils;
import org.hegemol.ymir.common.utils.NacosServiceNameUtils;
import org.hegemol.ymir.core.event.ServiceBeanExportEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Nacos服务注册
 *
 * @author KevinClair
 **/
public class NacosServiceRegister extends DefaultAbstractServiceRegister implements ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(NacosServiceRegister.class);

    private ApplicationEventPublisher applicationEventPublisher;
    private NamingService namingService;

    public NacosServiceRegister(NamingService namingService, Integer port, String protocol) {
        this.namingService = namingService;
        this.port = port;
        this.protocol = protocol;
    }

    @Override
    protected ServiceBeanCache getIfNoCache(final String name) throws RpcException {
        try {
            // 获取实例列表
            final List<Instance> instances = namingService.getAllInstances(name);
            if (CollectionUtils.isEmpty(instances)) {
                return null;
            }
            final Map<String, String> metadata = instances.get(0).getMetadata();
            ServiceBean serviceBean = new ServiceBean();
            serviceBean.setProtocol(metadata.get("protocol"));
            serviceBean.setAddress(metadata.get("address"));
            serviceBean.setWeight(Integer.valueOf(metadata.get("weight")));
            serviceBean.setGroup(metadata.get("group"));
            serviceBean.setVersion(metadata.get("version"));
            serviceBean.setName(metadata.get("name"));
            serviceBean.setClazz(metadata.get("clazz"));
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

    @Override
    public void setApplicationEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * 服务导出
     *
     * @param model            服务注册模型
     * @param exportEventModel 服务导出事件模型
     */
    protected void exportService(final ServiceBean model, final ServiceBeanEvent exportEventModel) throws Exception {
        final String serviceName = NacosServiceNameUtils.buildService(CommonConstant.SERVICE_PROVIDER_SIDE, model.getName());
        exportEventModel.setPath(serviceName);
        Instance instance = new Instance();
        // 解析ip地址
        instance.setIp(model.getAddress().split(":")[0]);
        instance.setPort(port);
        Map<String, String> metaData = new HashMap<>();
        metaData.put("protocol", model.getProtocol());
        metaData.put("address", model.getAddress());
        metaData.put("weight", String.valueOf(model.getWeight()));
        metaData.put("group", model.getGroup());
        metaData.put("version", model.getVersion());
        metaData.put("name", model.getName());
        metaData.put("clazz", model.getClazz());

        instance.setMetadata(metaData);
        namingService.registerInstance(serviceName, instance);
        logger.info("Service export to nacos success, register url:{}", GsonUtils.getInstance().toJson(model));
    }

    @Override
    protected void unRegisterBean() throws Exception {
        // TODO 删除注册节点
    }

    @Override
    protected void close() throws Exception {
        // TODO 客户端关闭
    }
}
