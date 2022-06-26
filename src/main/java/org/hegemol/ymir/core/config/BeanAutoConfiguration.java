package org.hegemol.ymir.core.config;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.hegemol.ymir.client.NettyClient;
import org.hegemol.ymir.client.proxy.ClientProxyFactory;
import org.hegemol.ymir.client.register.NacosServiceRegister;
import org.hegemol.ymir.client.register.ZookeeperServiceRegister;
import org.hegemol.ymir.common.constant.CommonConstant;
import org.hegemol.ymir.common.register.ServiceRegister;
import org.hegemol.ymir.common.utils.RegistryParseUtil;
import org.hegemol.ymir.core.ServiceExportProcessor;
import org.hegemol.ymir.core.annotation.ConditionalOnPropertyStartsWith;
import org.hegemol.ymir.core.handler.RequestHandler;
import org.hegemol.ymir.core.property.ConfigurationProperty;
import org.hegemol.ymir.core.property.RegisterCenterProperty;
import org.hegemol.ymir.server.NettyServer;
import org.hegemol.ymir.server.discovery.NacosServiceDiscovery;
import org.hegemol.ymir.server.discovery.ServiceDiscovery;
import org.hegemol.ymir.server.discovery.ZookeeperServiceDiscovery;
import org.hegemol.ymir.server.handler.NettyServerHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Properties;

import static org.hegemol.ymir.common.constant.CommonConstant.CONTEXT_SEP;
import static org.hegemol.ymir.common.constant.CommonConstant.DEFAULT_NAMESPACE;
import static org.hegemol.ymir.common.constant.CommonConstant.REGISTRY_TYPE_NACOS;

/**
 * 客户端Bean注入
 *
 * @author KevinClair
 */
@ConditionalOnProperty(value = "ymir.register.url")
@Configuration
public class BeanAutoConfiguration {


    /**
     * 注册YmirConfigurationProperty.
     *
     * @return
     */
    @Bean
    @ConfigurationProperties(prefix = "ymir")
    public ConfigurationProperty ymirConfigurationProperty(){
        return new ConfigurationProperty();
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
    @ConditionalOnPropertyStartsWith(prefix = "ymir.register", name = "url", havingStartsWithValue = "zookeeper")
    static class ZookeeperClientRegisterCenter{

        /**
         * Zk客户端curtor注册
         *
         * @param property zk客户端属性{@link RegisterCenterProperty}
         * @return {@link CuratorFramework}
         */
        @Bean
        public CuratorFramework curatorFramework(RegisterCenterProperty property){
            Map<String, String> parseParam = RegistryParseUtil.parseParam(property.getUrl(), CommonConstant.REGISTRY_TYPE_ZOOKEEPER);

            CuratorFramework client = CuratorFrameworkFactory.builder()
                    .connectString(parseParam.get("address"))
                    .connectionTimeoutMs(Integer.valueOf(parseParam.getOrDefault("connectionTimeout", "15000")))
                    .sessionTimeoutMs(Integer.valueOf(parseParam.getOrDefault("sessionTimeout", "60000")))
                    .retryPolicy(new ExponentialBackoffRetry(1000, Integer.valueOf(parseParam.getOrDefault("retryTimes", "3"))))
                    .namespace(DEFAULT_NAMESPACE)
                    .build();
            client.start();
            return client;
        }

        /**
         * zk服务注册器
         *
         * @param clientProperty 配置属性{@link ConfigurationProperty}
         * @param zkClient       zk客户端{@link CuratorFramework}
         * @return {@link ZookeeperServiceRegister}
         */
        @Bean
        public ServiceRegister zookeeperServiceRegister(ConfigurationProperty clientProperty, CuratorFramework zkClient){
            return new ZookeeperServiceRegister(zkClient, clientProperty.getPort(), clientProperty.getSerial());
        }

        /**
         * 服务发现
         *
         * @param zkClient zookeeper客户端
         * @return {@link ServiceDiscovery}
         */
        @Bean
        public ServiceDiscovery ymirServiceDiscovery(CuratorFramework zkClient, NettyClient client){
            return new ZookeeperServiceDiscovery(zkClient, client);
        }
    }

    /**
     * nacos注册中心注册器
     */
    @ConditionalOnPropertyStartsWith(prefix = "ymir.register", name = "url", havingStartsWithValue = "nacos")
    static class NacosClientRegisterCenter{

        /**
         * 初始化Nacos客户端
         *
         * @param property 注册中心
         * @return {@link NamingService}
         * @throws NacosException {@link NacosException}
         */
        @Bean
        public NamingService namingService(RegisterCenterProperty property) throws NacosException {
            // 地址示例nacos://xxx:8848,yyy:8848/namespace=*****?
            Map<String, String> parseParam = RegistryParseUtil.parseParam(property.getUrl(), REGISTRY_TYPE_NACOS);
            String addressInput = parseParam.get("address");
            int idx = addressInput.indexOf(CONTEXT_SEP);
            String namespace;
            String address;
            if (idx > 0) {
                address = addressInput.substring(0, idx);
                namespace = addressInput.substring(idx + 1);
                if (StringUtils.isBlank(namespace)) {
                    namespace = DEFAULT_NAMESPACE;
                }
            } else {
                address = addressInput;
                namespace = DEFAULT_NAMESPACE;
            }
            Properties nacosProperties = new Properties();
            // server address.
            nacosProperties.put(PropertyKeyConst.SERVER_ADDR, address);
            // name space.
            nacosProperties.put(PropertyKeyConst.NAMESPACE, namespace);
            // the nacos authentication username
            nacosProperties.put(PropertyKeyConst.USERNAME, parseParam.getOrDefault(PropertyKeyConst.USERNAME, ""));
            // the nacos authentication password
            nacosProperties.put(PropertyKeyConst.PASSWORD, parseParam.getOrDefault(PropertyKeyConst.PASSWORD, ""));
            // access key for namespace
            nacosProperties.put(PropertyKeyConst.ACCESS_KEY, parseParam.getOrDefault(PropertyKeyConst.ACCESS_KEY, ""));
            // secret key for namespace
            nacosProperties.put(PropertyKeyConst.SECRET_KEY, parseParam.getOrDefault(PropertyKeyConst.SECRET_KEY, ""));
            return NamingFactory.createNamingService(nacosProperties);
        }

        /**
         * nacos服务注册器
         *
         * @param clientProperty 配置属性{@link ConfigurationProperty}
         * @param namingService  nacos服务注册发现客户端{@link NamingService}
         * @return {@link ZookeeperServiceRegister}
         */
        @Bean
        public ServiceRegister nacosServiceRegister(ConfigurationProperty clientProperty, NamingService namingService){
            return new NacosServiceRegister(namingService, clientProperty.getPort(), clientProperty.getSerial());
        }

        /**
         * 服务发现
         *
         * @param namingService nacos客户端
         * @return {@link ServiceDiscovery}
         */
        @Bean
        public ServiceDiscovery ymirServiceDiscovery(NamingService namingService, NettyClient client){
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
     * @param property           配置属性{@link ConfigurationProperty}
     * @param nettyServerHandler Netty服务端处理器{@link NettyServerHandler}
     * @return {@link NettyServer}
     */
    @Bean
    public NettyServer ymirNettyServer(ConfigurationProperty property, NettyServerHandler nettyServerHandler){
        return new NettyServer(property, nettyServerHandler);
    }

    /**
     * Netty客户端
     *
     * @return {@link NettyClient}
     */
    @Bean
    public NettyClient nettyNetClient(){
        return new NettyClient();
    }

    /**
     * Ymir客户端代理工厂
     *
     * @param serviceDiscovery 服务发现
     * @param netClient        Netty客户端
     * @return {@link ClientProxyFactory}
     */
    @Bean
    public ClientProxyFactory ymirClientProxyFactory(ServiceDiscovery serviceDiscovery, NettyClient netClient, ConfigurationProperty property){
        return new ClientProxyFactory(serviceDiscovery, netClient, property);
    }

    /**
     * 服务导出处理器
     *
     * @param serviceRegister 服务注册{@link ServiceRegister}
     * @param nettyServer     Netty服务{@link NettyServer}
     * @return {@link ServiceExportProcessor}
     */
    @Bean
    public ServiceExportProcessor defaultServiceExportProcessor(ServiceRegister serviceRegister, NettyServer nettyServer, ClientProxyFactory proxyFactory, ConfigurationProperty property, ServiceDiscovery serviceDiscovery){
        return new ServiceExportProcessor(serviceRegister, nettyServer, proxyFactory, property, serviceDiscovery);
    }
}
