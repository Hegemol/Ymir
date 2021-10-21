package org.season.ymir.server.discovery;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.season.ymir.client.YmirClientCacheManager;
import org.season.ymir.client.YmirNettyClient;
import org.season.ymir.common.constant.CommonConstant;
import org.season.ymir.common.entity.ServiceBean;
import org.season.ymir.common.exception.RpcException;
import org.season.ymir.common.utils.GsonUtils;
import org.season.ymir.common.utils.NacosServiceNameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * nacos服务发现
 *
 * @author KevinClair
 **/
public class NacosServiceDiscovery extends DefaultAbstractServiceDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(NacosServiceDiscovery.class);

    private NamingService namingService;
    private YmirNettyClient nettyClient;

    public NacosServiceDiscovery(final NamingService namingService, final YmirNettyClient nettyClient) {
        this.namingService = namingService;
        this.nettyClient = nettyClient;
    }

    @Override
    protected void handleClient(List<ServiceBean> serviceList) {
        for (ServiceBean each : serviceList) {
            boolean clientIsExisted = YmirClientCacheManager.contains(each.getAddress());
            if (!clientIsExisted) {
                nettyClient.initClient(each.getAddress());
            }
        }
    }

    @Override
    public List<ServiceBean> findServiceListByRegisterCenter(String name) throws Exception {
        // 从nacos中获取存活的实例列表
        final List<Instance> instances = namingService.selectInstances(NacosServiceNameUtils.buildService(CommonConstant.SERVICE_PROVIDER_SIDE, name), true);
        if (CollectionUtils.isEmpty(instances)) {
            throw new RpcException(String.format("No provider available for service %s from nacos", name));
        }
        final List<ServiceBean> serviceBeans = instances.stream().map(instance -> {
            final Map<String, String> metadata = instance.getMetadata();
            ServiceBean serviceBean = new ServiceBean();
            serviceBean.setProtocol(metadata.get("protocol"));
            serviceBean.setAddress(metadata.get("address"));
            serviceBean.setWeight(Integer.valueOf(metadata.get("weight")));
            serviceBean.setGroup(metadata.get("group"));
            serviceBean.setVersion(metadata.get("version"));
            serviceBean.setName(metadata.get("name"));
            serviceBean.setClazz(metadata.get("clazz"));
            return serviceBean;
        }).collect(Collectors.toList());
        super.put(name, serviceBeans);
        return serviceBeans;
    }

    @Override
    public void listener(final List<String> serviceList, final String address) {
        serviceList.forEach(service -> {
            try {
                namingService.subscribe(NacosServiceNameUtils.buildService(CommonConstant.SERVICE_PROVIDER_SIDE, service), event -> {
                    if (event instanceof NamingEvent) {
                        // 获取节点变更事件
                        final NamingEvent namingEvent = (NamingEvent) event;
                        final String serviceName = namingEvent.getServiceName().split(CommonConstant.INSTANCE_DELIMITER)[1];
                        NacosServiceDiscovery.super.remove(serviceName);
                        // 获取所有实例，之后覆盖
                        final List<Instance> allInstances = namingEvent.getInstances();
                        NacosServiceDiscovery.super.put(serviceName, allInstances.stream().map(instance -> {
                            final Map<String, String> metadata = instance.getMetadata();
                            ServiceBean serviceBean = new ServiceBean();
                            serviceBean.setProtocol(metadata.get("protocol"));
                            serviceBean.setAddress(metadata.get("address"));
                            serviceBean.setWeight(Integer.valueOf(metadata.get("weight")));
                            serviceBean.setGroup(metadata.get("group"));
                            serviceBean.setVersion(metadata.get("version"));
                            serviceBean.setName(metadata.get("name"));
                            serviceBean.setClazz(metadata.get("clazz"));
                            return serviceBean;
                        }).collect(Collectors.toList()));

                        // 写入consumer节点
                        Instance consumerInstance = new Instance();
                        final String[] addressSplit = address.split(CommonConstant.INSTANCE_DELIMITER);

                        consumerInstance.setIp(addressSplit[0]);
                        consumerInstance.setPort(Integer.parseInt(addressSplit[1]));
                        consumerInstance.setServiceName(serviceName);

                        try {
                            namingService.registerInstance(NacosServiceNameUtils.buildService(CommonConstant.SERVICE_CONSUMER_SIDE, serviceName), consumerInstance);
                        } catch (NacosException e) {
                            logger.error("Register consumer node error,serviceName:{}, instance:{}", serviceName, GsonUtils.getInstance().toJson(consumerInstance));
                        }
                    }
                });

            } catch (NacosException e) {
                logger.error("Nacos instance add listener error, message:{}", ExceptionUtils.getStackTrace(e));
                throw new RpcException(e);
            }
        });
    }
}
