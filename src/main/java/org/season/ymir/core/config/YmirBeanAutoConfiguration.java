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

    /**
     * Zk客户端Bean注册
     *
     * @param zookeeperClientProperty zk客户端属性{@link YmirZookeeperRegisterCenterProperty}
     * @return {@link ZkClient}
     */
    @Bean
    @ConditionalOnProperty(value = "ymir.zookeeper.url")
    public ZkClient zkClient(YmirZookeeperRegisterCenterProperty zookeeperClientProperty ){
        ZkClient zkClient = new ZkClient(zookeeperClientProperty.getUrl(), zookeeperClientProperty.getSessionTimeout(), zookeeperClientProperty.getConnectionTimeout());
        zkClient.setZkSerializer(new ZookeeperSerializer());
        return zkClient;
    }

    /**
     * zk服务注册器
     *
     * @param clientProperty 配置属性{@link YmirConfigurationProperty}
     * @param zkClient       zk客户端{@link ZkClient}
     * @return {@link ZookeeperServiceRegister}
     */
    @Bean
    @ConditionalOnBean(value = {YmirConfigurationProperty.class, ZkClient.class})
    public ZookeeperServiceRegister zookeeperServiceRegister(YmirConfigurationProperty clientProperty, ZkClient zkClient){
        return new ZookeeperServiceRegister(zkClient, clientProperty.getPort(), clientProperty.getProtocol(), 100);
    }

    /**
     * RequestHandler注册
     *
     * @param serviceRegister 服务注册器{@link ServiceRegister}
     * @param clientProperty  配置属性{@link YmirConfigurationProperty}
     * @return {@link RequestHandler}
     */
    @Bean
    @ConditionalOnBean(value = {ServiceRegister.class, YmirConfigurationProperty.class})
    public RequestHandler requestHandler(ServiceRegister serviceRegister, YmirConfigurationProperty clientProperty){
        return new RequestHandler(getMessageProtocol(clientProperty.getProtocol()), serviceRegister);
    }

    /**
     * Netty服务注册
     *
     * @param property       配置属性{@link YmirConfigurationProperty}
     * @param requestHandler 注册注册器{@link RequestHandler}
     * @return {@link YmirNettyServer}
     */
    @Bean
    @ConditionalOnBean(value = {YmirConfigurationProperty.class, RequestHandler.class})
    public YmirNettyServer ymirNettyServer(YmirConfigurationProperty property, RequestHandler requestHandler){
        return new YmirNettyServer(property, requestHandler);
    }

    /**
     * 服务到处处理器
     *
     * @param serviceRegister 服务注册{@link ServiceRegister}
     * @param nettyServer     Netty服务{@link YmirNettyServer}
     * @return {@link DefaultServiceExportProcessor}
     */
    @Bean
    @ConditionalOnBean(value = {ServiceRegister.class, YmirNettyServer.class})
    public DefaultServiceExportProcessor defaultServiceExportProcessor(ServiceRegister serviceRegister, YmirNettyServer nettyServer){
        return new DefaultServiceExportProcessor(serviceRegister, nettyServer);
    }

    /**
     * 获取消息序列化，此处通过模拟Dubbo的SPI机制进行获取对应的实现；
     *
     * @param name 序列化协议名
     * @return {@link MessageProtocol}
     */
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
