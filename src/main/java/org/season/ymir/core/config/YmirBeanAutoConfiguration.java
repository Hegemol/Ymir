package org.season.ymir.core.config;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.season.ymir.client.YmirNettyClient;
import org.season.ymir.client.proxy.YmirClientProxyFactory;
import org.season.ymir.client.register.NacosServiceRegister;
import org.season.ymir.client.register.ZookeeperServiceRegister;
import org.season.ymir.common.register.ServiceRegister;
import org.season.ymir.core.YmirServiceExportProcessor;
import org.season.ymir.core.handler.RequestHandler;
import org.season.ymir.core.property.RegisterCenterProperty;
import org.season.ymir.core.property.YmirConfigurationProperty;
import org.season.ymir.core.protocol.MessageProtocol;
import org.season.ymir.server.YmirNettyServer;
import org.season.ymir.server.discovery.ServiceDiscovery;
import org.season.ymir.server.discovery.ZookeeperServiceDiscovery;
import org.season.ymir.server.handler.NettyServerHandler;
import org.season.ymir.spi.loader.ExtensionLoader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 客户端Bean注入
 *
 * @author KevinClair
 */
@EnableConfigurationProperties({YmirConfigurationProperty.class, RegisterCenterProperty.class})
@ConditionalOnProperty(value = "ymir.register.url")
public class YmirBeanAutoConfiguration {

    @ConditionalOnProperty(value = "ymir.register.type", havingValue = "zookeeper")
    static class ZookeeperClientRegisterCenter{

        /**
         * Zk客户端curtor注册
         *
         * @param zookeeperClientProperty zk客户端属性{@link RegisterCenterProperty}
         * @return {@link CuratorFramework}
         */
        @Bean
        public CuratorFramework curatorFramework(RegisterCenterProperty zookeeperClientProperty ){
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
        public ServiceRegister zookeeperServiceRegister(YmirConfigurationProperty clientProperty, CuratorFramework zkClient){
            return new ZookeeperServiceRegister(zkClient, clientProperty.getPort(), clientProperty.getProtocol());
        }

        /**
         * 服务发现
         *
         * @param zkClient zookeeper客户端
         * @return {@link ServiceDiscovery}
         */
        @Bean
        public ServiceDiscovery ymirServiceDiscovery(CuratorFramework zkClient, YmirNettyClient client){
            return new ZookeeperServiceDiscovery(zkClient, client);
        }
    }

    static class NacosClientRegisterCenter{

        // TODO nacos客户端

        /**
         * nacos服务注册器
         *
         * @param clientProperty 配置属性{@link YmirConfigurationProperty}
         * @param nacosClient       zk客户端{@link CuratorFramework}
         * @return {@link ZookeeperServiceRegister}
         */
        @Bean
        public ServiceRegister zookeeperServiceRegister(YmirConfigurationProperty clientProperty){
            return new NacosServiceRegister();
        }
    }

    /**
     * RequestHandler注册
     *
     * @param serviceRegister 服务注册器{@link ServiceRegister}
     * @return {@link RequestHandler}
     */
    @Bean
    public RequestHandler requestHandler(ServiceRegister serviceRegister){
        return new RequestHandler(serviceRegister);
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
     * Netty客户端
     *
     * @return {@link YmirNettyClient}
     */
    @Bean
    public YmirNettyClient nettyNetClient(YmirConfigurationProperty property){
        return new YmirNettyClient(ExtensionLoader.getExtensionLoader(MessageProtocol.class).getLoader(property.getProtocol()));
    }

    /**
     * Ymir客户端代理工厂
     *
     * @param serviceDiscovery 服务发现
     * @param netClient        Netty客户端
     * @return {@link YmirClientProxyFactory}
     */
    @Bean
    public YmirClientProxyFactory ymirClientProxyFactory(ServiceDiscovery serviceDiscovery, YmirNettyClient netClient){
        return new YmirClientProxyFactory(serviceDiscovery, netClient);
    }

    /**
     * 服务导出处理器
     *
     * @param serviceRegister 服务注册{@link ServiceRegister}
     * @param nettyServer     Netty服务{@link YmirNettyServer}
     * @return {@link YmirServiceExportProcessor}
     */
    @Bean
    public YmirServiceExportProcessor defaultServiceExportProcessor(ServiceRegister serviceRegister, YmirNettyServer nettyServer, YmirClientProxyFactory proxyFactory, YmirConfigurationProperty property, ServiceDiscovery serviceDiscovery){
        return new YmirServiceExportProcessor(serviceRegister, nettyServer, proxyFactory, property, serviceDiscovery);
    }
}
