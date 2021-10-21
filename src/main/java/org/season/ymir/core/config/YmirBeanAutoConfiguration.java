package org.season.ymir.core.config;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
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
import org.season.ymir.server.discovery.NacosServiceDiscovery;
import org.season.ymir.server.discovery.ServiceDiscovery;
import org.season.ymir.server.discovery.ZookeeperServiceDiscovery;
import org.season.ymir.server.handler.NettyServerHandler;
import org.season.ymir.spi.loader.ExtensionLoader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.Properties;

/**
 * 客户端Bean注入
 *
 * @author KevinClair
 */
@ConditionalOnProperty(value = "ymir.register.url")
@Configuration
public class YmirBeanAutoConfiguration {


    /**
     * 注册YmirConfigurationProperty.
     *
     * @return
     */
    @Bean
    @ConfigurationProperties(prefix = "ymir")
    public YmirConfigurationProperty ymirConfigurationProperty(){
        return new YmirConfigurationProperty();
    }

    /**
     * 注册RegisterCenterProperty.
     *
     * @return
     */
    @Bean
    @ConfigurationProperties(prefix = "ymir.register")
    public RegisterCenterProperty registerCenterProperty(){
        return new RegisterCenterProperty();
    }

    /**
     * zookeeper注册中心注册器
     */
    @ConditionalOnProperty(prefix = "ymir.register", name = "type", havingValue = "zookeeper")
    static class ZookeeperClientRegisterCenter{

        /**
         * Zk客户端curtor注册
         *
         * @param zookeeperClientProperty zk客户端属性{@link RegisterCenterProperty}
         * @return {@link CuratorFramework}
         */
        @Bean
        public CuratorFramework curatorFramework(RegisterCenterProperty zookeeperClientProperty ){
            final Properties props = zookeeperClientProperty.getProps();

            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, Integer.valueOf(props.getProperty("retryTimes", "3")));
            CuratorFramework client = CuratorFrameworkFactory.builder()
                    .connectString(zookeeperClientProperty.getUrl())
                    .connectionTimeoutMs(Integer.valueOf(props.getProperty("connectionTimeout", "6000")))
                    .sessionTimeoutMs(Integer.valueOf(props.getProperty("sessionTimeout", "6000")))
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

    /**
     * nacos注册中心注册器
     */
    @ConditionalOnProperty(prefix = "ymir.register", name = "type", havingValue = "nacos")
    static class NacosClientRegisterCenter{

        /**
         * 初始化Nacos客户端
         *
         * @param nacosCenterProperty 注册中心
         * @return {@link NamingService}
         * @throws NacosException {@link NacosException}
         */
        @Bean
        public NamingService namingService(RegisterCenterProperty nacosCenterProperty) throws NacosException {
            Properties nacosProperties = new Properties();
            // server address.
            nacosProperties.put(PropertyKeyConst.SERVER_ADDR, nacosCenterProperty.getUrl());
            final Properties props = nacosCenterProperty.getProps();
            if (Objects.isNull(props)){
                return NamingFactory.createNamingService(nacosProperties);
            }
            // name space.
            nacosProperties.put(PropertyKeyConst.NAMESPACE, props.getProperty(PropertyKeyConst.NAMESPACE, ""));
            // the nacos authentication username
            nacosProperties.put(PropertyKeyConst.USERNAME, props.getProperty(PropertyKeyConst.USERNAME, ""));
            // the nacos authentication password
            nacosProperties.put(PropertyKeyConst.PASSWORD, props.getProperty(PropertyKeyConst.PASSWORD, ""));
            // access key for namespace
            nacosProperties.put(PropertyKeyConst.ACCESS_KEY, props.getProperty(PropertyKeyConst.ACCESS_KEY, ""));
            // secret key for namespace
            nacosProperties.put(PropertyKeyConst.SECRET_KEY, props.getProperty(PropertyKeyConst.SECRET_KEY, ""));
            return NamingFactory.createNamingService(nacosProperties);
        }

        /**
         * nacos服务注册器
         *
         * @param clientProperty 配置属性{@link YmirConfigurationProperty}
         * @param namingService  nacos服务注册发现客户端{@link NamingService}
         * @return {@link ZookeeperServiceRegister}
         */
        @Bean
        public ServiceRegister nacosServiceRegister(YmirConfigurationProperty clientProperty, NamingService namingService){
            return new NacosServiceRegister(namingService, clientProperty.getPort(), clientProperty.getProtocol());
        }

        /**
         * 服务发现
         *
         * @param namingService nacos客户端
         * @return {@link ServiceDiscovery}
         */
        @Bean
        public ServiceDiscovery ymirServiceDiscovery(NamingService namingService, YmirNettyClient client){
            return new NacosServiceDiscovery(namingService, client);
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
