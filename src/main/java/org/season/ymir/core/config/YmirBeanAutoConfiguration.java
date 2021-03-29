package org.season.ymir.core.config;

import org.I0Itec.zkclient.ZkClient;
import org.season.ymir.client.process.DefaultServiceExportProcessor;
import org.season.ymir.client.register.ZookeeperServiceRegister;
import org.season.ymir.common.register.ServiceRegister;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 客户端Bean注入
 *
 * @author KevinClair
 */
@EnableConfigurationProperties({YmirConfigurationProperty.class, YmirZookeeperRegisterCenterProperty.class})
public class YmirBeanAutoConfiguration {

    @Bean
    @ConditionalOnProperty(value = "ymir.zookeeper.url")
    public ZkClient zkClient(YmirZookeeperRegisterCenterProperty zookeeperClientProperty ){
        return new ZkClient(zookeeperClientProperty.getUrl(), zookeeperClientProperty.getSessionTimeout(), zookeeperClientProperty.getConnectionTimeout());
    }

    @Bean
    @ConditionalOnBean(value = {YmirConfigurationProperty.class, ZkClient.class})
    public ZookeeperServiceRegister zookeeperServiceRegister(YmirConfigurationProperty clientProperty, ZkClient zkClient){
        return new ZookeeperServiceRegister(zkClient, clientProperty.getPort(), clientProperty.getProtocol(), 100);
    }

    @Bean
    @ConditionalOnBean(value = {ServiceRegister.class})
    public DefaultServiceExportProcessor defaultServiceExportProcessor(ServiceRegister serviceRegister){
        return new DefaultServiceExportProcessor(serviceRegister);
    }
}
