package org.season.ymir.core.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.season.ymir.client.process.DefaultServiceExportProcessor;
import org.season.ymir.client.proxy.YmirClientProxyFactory;
import org.season.ymir.client.register.ZookeeperServiceRegister;
import org.season.ymir.common.exception.RpcException;
import org.season.ymir.common.register.ServiceRegister;
import org.season.ymir.core.handler.MessageProtocol;
import org.season.ymir.core.handler.RequestHandler;
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
     * Zk客户端curtor注册
     *
     * @param zookeeperClientProperty zk客户端属性{@link YmirZookeeperRegisterCenterProperty}
     * @return {@link CuratorFramework}
     */
    @Bean
    @ConditionalOnProperty(value = "ymir.zookeeper.url")
    public CuratorFramework curatorFramework(YmirZookeeperRegisterCenterProperty zookeeperClientProperty ){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,zookeeperClientProperty.getRetryTimes());
        CuratorFramework client = CuratorFrameworkFactory.newClient(zookeeperClientProperty.getUrl(),retryPolicy);
        client.start();
        return client;
    }

    /**
     * zk服务注册器
     *
     * @param clientProperty 配置属性{@link YmirConfigurationProperty}
     * @param zkClient       zk客户端{@link CuratorFramework}
     * @return {@link ZookeeperServiceRegister}
     */
    @Bean
    @ConditionalOnBean(value = {YmirConfigurationProperty.class, CuratorFramework.class})
    public ZookeeperServiceRegister zookeeperServiceRegister(YmirConfigurationProperty clientProperty, CuratorFramework zkClient){
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
    @ConditionalOnBean(value = {ServiceRegister.class, YmirNettyServer.class, YmirClientProxyFactory.class})
    public DefaultServiceExportProcessor defaultServiceExportProcessor(ServiceRegister serviceRegister, YmirNettyServer nettyServer, YmirClientProxyFactory proxyFactory){
        return new DefaultServiceExportProcessor(serviceRegister, nettyServer, proxyFactory);
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
