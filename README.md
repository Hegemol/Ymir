# Ymir
## 背景
Ymir，出自动漫[进击的巨人](https://baike.baidu.com/item/%E8%BF%9B%E5%87%BB%E7%9A%84%E5%B7%A8%E4%BA%BA/65641?fr=aladdin) 中的角色，始祖巨人尤米尔。因为很喜欢这个动漫，所以以这个命名了这个项目。

## Ymir是什么
一个Rpc框架的示例项目，大部分思路都来源于[Dubbo](http://dubbo.apache.org)的设计思想，主要的功能是基于Netty的服务之间的调用。大部分思想都是借鉴于Dubbo的一些设计理念，例如服务上传以及注册，包括SPI加载类等等。

## Ymir的基础架构
* 保持了和Dubbo一致的架构设计，区别在于去掉了Monitor的概念，关于服务监控，后面有时间会进行补充；
    * Provider启动，向Registry上传服务注册信息；
    * Consumer启动，先订阅所有需要的Provider信息，拉取所有的Provider信息落到本地缓存，之后注册Consumer节点；
    * Registry通知Provider，已经有Consumer接入；
    * Consumer向Provider发起请求；
  ![](./images/Ymir架构设计.png)

## Ymir有哪些功能

#### 服务暴露
* 服务注册至注册中心(可选不注册)
* 服务权重(doing)
#### 服务引用
* 启动时检查(doing)
* 每次调用时的负载均衡实现
* 服务调用超时检测
* 服务重试(doing)
* 服务提供者直连

## 如何使用
* 克隆代码到本地仓库
`git clone https://github.com/KevinClair/Ymir.git`
* 打包到本地Maven仓库
`mvn clean install -Dmaven.test.skip=true`
* 在需要使用的项目中添加Maven依赖
```java
<dependency>
    <groupId>org.season</groupId>
    <artifactId>ymir</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
#### 服务端
* 编写接口
```java
public interface TestService {
    String test(String name);
}
```
* 在需要暴露的服务上添加注解
```java
@YmirService
public class TestServiceImpl implements TestService {
    @Override
    public String test(String name) {
        return "Hello "+name;
    }
}
```

#### 客户端
* 添加接口对应的Maven依赖
```java
<dependency>
    <groupId>org.season</groupId>
    <artifactId>ymir-example-common</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
* 服务引用
```java
@RestController
public class TestController {

    @YmirReference
    private TestService service;

    @PostMapping("/name")
    public String get(@RequestParam("name") String name){
        return service.test(name);
    }
}
```
* 发送请求，返回结果
```java
curl --location --request POST 'http://localhost:port/name?name=11'
```
## Ymir的一些设计理念
#### SPI
#### 服务注册
#### 服务发现
#### Netty请求
## 有问题反馈
在使用中有任何问题，欢迎反馈给我，可以用以下联系方式跟我交流
* Email: kevinclair@apache.org

## 感激
感谢以下的项目，在个人学习过程中，给我起到了很大的帮助

* [Dubbo](http://dubbo.apache.org/zh-cn/)
* [shenyu](https://github.com/apache/incubator-shenyu)

## 最近计划
最近准备开始着手做一次项目的重构和升级，调整一下项目的模块以及工程架构；
* Netty优化，粘包拆包；
* 取消SpringBoot父框架；
* YmirService属性扩展，增加group等属性；
* YmirReference属性扩展，增加group等属性；
* Filter过滤器；
* 项目结构优化，包路径重构；
* 第二次测试；
