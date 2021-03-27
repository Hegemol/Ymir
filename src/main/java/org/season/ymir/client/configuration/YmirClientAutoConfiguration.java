package org.season.ymir.client.configuration;

import org.season.ymir.client.register.ZookeeperServiceRegister;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 客户端Bean注入
 *
 * @author KevinClair
 */
@EnableConfigurationProperties(YmirClientPropertySource.class)
public class YmirClientAutoConfiguration {

    @Bean
    public ZookeeperServiceRegister zookeeperServiceRegister(YmirClientPropertySource propertySource){
        return new ZookeeperServiceRegister(propertySource.getAddress(), 20777, propertySource.getPort(), 100);
    }
}
