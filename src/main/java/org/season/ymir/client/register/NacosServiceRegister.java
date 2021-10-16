package org.season.ymir.client.register;

import org.season.ymir.common.register.DefaultAbstractServiceRegister;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

/**
 * Nacos服务注册
 *
 * @author KevinClair
 **/
public class NacosServiceRegister extends DefaultAbstractServiceRegister implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
