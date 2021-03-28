package org.season.ymir.client.configuration;

import org.I0Itec.zkclient.ZkClient;
import org.season.ymir.client.process.DefaultServiceExportProcessor;
import org.season.ymir.client.register.ZookeeperServiceRegister;
import org.season.ymir.common.register.ServiceRegister;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 客户端Bean注入
 *
 * @author KevinClair
 */
@EnableConfigurationProperties({YmirClientProperty.class, YmirZookeeperClientProperty.class})
public class YmirClientAutoConfiguration {

    @Bean
    public ZkClient zkClient(YmirZookeeperClientProperty zookeeperClientProperty ){
        return new ZkClient(zookeeperClientProperty.getUrl(), zookeeperClientProperty.getSessionTimeout(), zookeeperClientProperty.getConnectionTimeout());
    }

    @Bean
    public ZookeeperServiceRegister zookeeperServiceRegister(YmirClientProperty clientProperty, ZkClient zkClient){
        return new ZookeeperServiceRegister(zkClient, 20777, clientProperty.getPort(), 100);
    }

    @Bean
    public DefaultServiceExportProcessor defaultServiceExportProcessor(ServiceRegister serviceRegister){
        return new DefaultServiceExportProcessor(serviceRegister);
    }
}
