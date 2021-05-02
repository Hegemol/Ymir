package org.season.ymir.core.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.season.ymir.client.net.NettyNetClient;
import org.season.ymir.client.process.DefaultServiceExportProcessor;
import org.season.ymir.client.proxy.YmirClientProxyFactory;
import org.season.ymir.client.register.ZookeeperServiceRegister;
import org.season.ymir.common.exception.RpcException;
import org.season.ymir.common.register.ServiceRegister;
import org.season.ymir.core.balance.LoadBalance;
import org.season.ymir.core.discovery.YmirServiceDiscovery;
import org.season.ymir.core.discovery.ZookeeperYmirServiceDiscovery;
import org.season.ymir.core.handler.RequestHandler;
import org.season.ymir.core.protocol.MessageProtocol;
import org.season.ymir.server.YmirNettyServer;
import org.season.ymir.server.handle.NettyServerHandler;
import org.season.ymir.spi.annodation.SPI;
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
@ConditionalOnProperty(value = "ymir.zookeeper.url")
public class YmirBeanAutoConfiguration {

    /**
     * Zk客户端curtor注册
     *
     * @param zookeeperClientProperty zk客户端属性{@link YmirZookeeperRegisterCenterProperty}
     * @return {@link CuratorFramework}
     */
    @Bean
    public CuratorFramework curatorFramework(YmirZookeeperRegisterCenterProperty zookeeperClientProperty ){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,zookeeperClientProperty.getRetryTimes());
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(zookeeperClientProperty.getUrl())
                .connectionTimeoutMs(zookeeperClientProperty.getConnectionTimeout())
                .sessionTimeoutMs(zookeeperClientProperty.getSessionTimeout())
                .retryPolicy(retryPolicy)
                .namespace("ymir")
                .build();
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
    public ZookeeperServiceRegister zookeeperServiceRegister(YmirConfigurationProperty clientProperty, CuratorFramework zkClient){
        return new ZookeeperServiceRegister(zkClient, clientProperty.getPort(), clientProperty.getProtocol());
    }

    /**
     * RequestHandler注册
     *
     * @param serviceRegister 服务注册器{@link ServiceRegister}
     * @param clientProperty  配置属性{@link YmirConfigurationProperty}
     * @return {@link RequestHandler}
     */
    @Bean
    public RequestHandler requestHandler(ServiceRegister serviceRegister, YmirConfigurationProperty clientProperty){
        return new RequestHandler(getMessageProtocol(clientProperty.getProtocol()), serviceRegister);
    }

    /**
     * Netty服务端处理器
     *
     * @param requestHandler 请求处理器{@link RequestHandler}
     * @return Netty服务端处理器 {@link NettyServerHandler}
     */
    @Bean
    public NettyServerHandler nettyServerHandler(RequestHandler requestHandler){
        return new NettyServerHandler(requestHandler);
    }

    /**
     * Netty服务注册
     *
     * @param property           配置属性{@link YmirConfigurationProperty}
     * @param nettyServerHandler Netty服务端处理器{@link NettyServerHandler}
     * @return {@link YmirNettyServer}
     */
    @Bean
    public YmirNettyServer ymirNettyServer(YmirConfigurationProperty property, NettyServerHandler nettyServerHandler){
        return new YmirNettyServer(property, nettyServerHandler);
    }

    /**
     * 服务发现
     *
     * @param zkClient zookeeper客户端
     * @return {@link YmirServiceDiscovery}
     */
    @Bean
    public YmirServiceDiscovery ymirServiceDiscovery(CuratorFramework zkClient){
        return new ZookeeperYmirServiceDiscovery(zkClient);
    }

    /**
     * Netty客户端
     *
     * @return {@link NettyNetClient}
     */
    @Bean
    public NettyNetClient nettyNetClient(){
        return new NettyNetClient();
    }

    /**
     * Ymir客户端代理工厂
     *
     * @param serviceDiscovery 服务发现
     * @param netClient        Netty客户端
     * @param property         配置信息
     * @return {@link YmirClientProxyFactory}
     */
    @Bean
    public YmirClientProxyFactory ymirClientProxyFactory(YmirServiceDiscovery serviceDiscovery, NettyNetClient netClient, YmirConfigurationProperty property){
        return new YmirClientProxyFactory(serviceDiscovery, netClient, getMessageProtocol(property.getProtocol()), getLoadBalance(property.getLoadBalance()));
    }

    /**
     * 服务导出处理器
     *
     * @param serviceRegister 服务注册{@link ServiceRegister}
     * @param nettyServer     Netty服务{@link YmirNettyServer}
     * @return {@link DefaultServiceExportProcessor}
     */
    @Bean
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

    /**
     * 使用spi匹配符合配置的负载均衡算法
     *
     * @param name
     * @return {@link LoadBalance}
     */
    private LoadBalance getLoadBalance(String name) {
        ServiceLoader<LoadBalance> loader = ServiceLoader.load(LoadBalance.class);
        Iterator<LoadBalance> iterator = loader.iterator();
        while (iterator.hasNext()) {
            LoadBalance loadBalance = iterator.next();
            SPI spi = loadBalance.getClass().getAnnotation(SPI.class);
            if (name.equals(spi.value())) {
                return loadBalance;
            }
        }
        throw new RpcException("invalid load balance config");
    }
}
