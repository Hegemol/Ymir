package org.season.ymir.core.config;

import org.I0Itec.zkclient.ZkClient;
import org.season.ymir.client.process.DefaultServiceExportProcessor;
import org.season.ymir.client.register.ZookeeperServiceRegister;
import org.season.ymir.common.exception.RpcException;
import org.season.ymir.common.register.ServiceRegister;
import org.season.ymir.core.handler.MessageProtocol;
import org.season.ymir.core.handler.RequestHandler;
import org.season.ymir.core.zookeeper.ZookeeperSerializer;
import org.season.ymir.server.YmirNettyServer;
import org.season.ymir.spi.annodation.SPI;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.Iterator;
import java.util.ServiceLoader;

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
        ZkClient zkClient = new ZkClient(zookeeperClientProperty.getUrl(), zookeeperClientProperty.getSessionTimeout(), zookeeperClientProperty.getConnectionTimeout());
        zkClient.setZkSerializer(new ZookeeperSerializer());
        return zkClient;
    }

    @Bean
    @ConditionalOnBean(value = {YmirConfigurationProperty.class, ZkClient.class})
    public ZookeeperServiceRegister zookeeperServiceRegister(YmirConfigurationProperty clientProperty, ZkClient zkClient){
        return new ZookeeperServiceRegister(zkClient, clientProperty.getPort(), clientProperty.getProtocol(), 100);
    }

    @Bean
    @ConditionalOnBean(value = {ServiceRegister.class, YmirConfigurationProperty.class})
    public RequestHandler requestHandler(ServiceRegister serviceRegister, YmirConfigurationProperty clientProperty){
        return new RequestHandler(getMessageProtocol(clientProperty.getProtocol()), serviceRegister);
    }

    @Bean
    @ConditionalOnBean(value = {YmirConfigurationProperty.class, RequestHandler.class})
    public YmirNettyServer ymirNettyServer(YmirConfigurationProperty property, RequestHandler requestHandler){
        return new YmirNettyServer(property, requestHandler);
    }

    @Bean
    @ConditionalOnBean(value = {ServiceRegister.class, YmirNettyServer.class})
    public DefaultServiceExportProcessor defaultServiceExportProcessor(ServiceRegister serviceRegister, YmirNettyServer nettyServer){
        return new DefaultServiceExportProcessor(serviceRegister, nettyServer);
    }

    private MessageProtocol getMessageProtocol(String name) {
        // TODO 此处SPI数据改造
        ServiceLoader<MessageProtocol> loader = ServiceLoader.load(MessageProtocol.class);
        Iterator<MessageProtocol> iterator = loader.iterator();
        while (iterator.hasNext()) {
            MessageProtocol messageProtocol = iterator.next();
            SPI spi = messageProtocol.getClass().getAnnotation(SPI.class);
            if (name.equals(spi.value())) {
                return messageProtocol;
            }
        }
        throw new RpcException("invalid message protocol config!");
    }
}
