package org.season.ymir.server.discovery;

import org.season.ymir.common.entity.ServiceBean;

import java.util.List;

/**
 * nacos服务发现
 *
 * @author KevinClair
 **/
public class NacosServiceDiscovery extends DefaultAbstractServiceDiscovery{

    @Override
    protected void handleClient(final List<ServiceBean> serviceList) {

    }

    @Override
    public void listener(final List<String> serviceList, final String address) {

    }
}
