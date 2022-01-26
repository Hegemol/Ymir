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
* 启动时检查
* 负载均衡
* 泛化调用
* SPI
* 超时检测
* 服务提供者直连
* 服务分组(doing)
* 多版本(doing)
* 注册事件通知
* 隐式传参
* 心跳检测
* 异步调用
* 调用过滤

## 如何使用
### 添加依赖
* 克隆代码到本地仓库
`git clone https://github.com/KevinClair/Ymir.git`
* 打包到本地Maven仓库
`mvn clean install -Dmaven.test.skip=true`
* 在需要使用的项目中添加Maven依赖
```xml
<dependency>
    <groupId>org.season</groupId>
    <artifactId>ymir</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
### 配置信息
#### 配置文件信息
* Ymir共支持以下几种类型的配置信息
```text
/**
 * 服务端口,不填默认为20777
 */
private Integer port = 20777;

/**
 * 服务序列化协议, 不日按默认为protostuff
 */
private String serial = "protostuff";
```
* Ymir对注册中心的支持;
  * 目前已支持Zookeeper和Nacos;
  * 预留接口[ServiceDiscovery](src/main/java/org/season/ymir/server/discovery/ServiceDiscovery.java)以及[ServiceRegister](src/main/java/org/season/ymir/common/register/ServiceRegister.java);
  * 新增加的注册中心只需要实现上面两个接口就可以无缝对接;
```yaml
ymir:
  register:
    // 目前可选注册类型zookeeper和nacos
    type: zookeeper 
    // 注册中心地址，集群用`,`分隔
    url: localhost:2181
    // 客户端连接参数，一般为连接超时时间等
    props:
      connectionTimeout: 6000
```
#### 注解支持
* @Service
```java
public @interface Service {

    /**
     * 权重
     */
    int weight() default 0;

    /**
     * 是否需要注册
     */
    boolean register() default true;

    /**
     * 分组
     */
    String group() default "";

    /**
     * 版本
     */
    String version() default "";
}
```
* @Reference
```java
public @interface Reference {

    /**
     * 启动时检查
     */
    boolean check() default false;

    /**
     * 负载均衡
     */
    String loadBalance() default "random";

    /**
     * 超时时间
     */
    int timeout() default 3000;

    /**
     * 重试次数
     */
    int retries() default 2;

    /**
     * 服务直连url
     */
    String url() default "";

    /**
     * 过滤器
     */
    String filter() default "";
  
    /**
     * 是否开启异步调用
     */
    boolean async() default false;
}
```
### 开始使用
#### 服务端
* 编写接口
```java
public interface TestService {
    String test(String name);
}
```
* 在需要暴露的服务上添加注解
```java
@Service
public class TestServiceImpl implements TestService {
    @Override
    public String test(String name) {
        return "Hello "+name;
    }
}
```

#### 客户端
* 添加接口对应的Maven依赖
```xml
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

    @Reference
    private TestService service;

    @PostMapping("/name")
    public String get(@RequestParam("name") String name) {
      return service.test(name);
    }
}
```

* 发送请求，返回结果
```shell
curl --location --request POST 'http://localhost:port/name?name=11'
```

## Ymir的一些设计理念
### ymir协议
#### 详情
* ymir基于本身的数据传递规则，设计了自己的消息协议，具体规则为
  * magic code(魔法值)，占用4个字节；
  * full length(body长度)，代表整个消息体的长度数据；
  * type，代表本次的消息类型，具体请查看枚举[MessageTypeEnum](src/main/java/org/season/ymir/common/base/MessageTypeEnum.java)；
  * serial，代表本次的消息序列化类型，具体请查看枚举[SerializationTypeEnum](src/main/java/org/season/ymir/common/base/SerializationTypeEnum.java)；
  * requestId，代表本次请求的请求id，由客户端生成；
  * 请求body；
```text
 *   0     1     2     3     4     5     6     7     8     9     10     11    12    13    14
 *   +-----+-----+-----+-----+----—+-----+-----+-----+-----+------+-----+-----+-----+-----+
 *   |   magic   code        |      full length      | type|serial|       requestId       |
 *   +-----------------------+-----------------------+-----+------+-----------------------+
 *   |                                                                                    |
 *   |                                       body                                         |
 *   |                                                                                    |
 *   |                                                                                    |
 *   +------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   4B requestId（请求的Id）    1B type（消息类型）
 * 1B serial（序列化类型）    4B  full length（消息长度）
 * body（object类型数据）
```
### 泛化调用
#### 如何使用
* Ymir的泛化调用允许客户端不依赖服务端的依赖就可以调用服务。在需要使用的地方添加[GenericService](src/main/java/org/season/ymir/core/generic/GenericService.java)
的引入即可;
```java

@RestController
public class TestController {

  @Reference
  private GenericService service;

  @PostMapping("/name")
    public String get(@RequestParam("name") String name){
        return service.invoke("org.season.ymir.example.client.controller.TestInterface", "test", new String[]{"java.lang.String"}, new Object[]{name});
    }
}
```
* 在invoke的方法中填入参数就可以通过泛化调用请求服务;
### 隐式传参
#### 如何使用
* Ymir的服务允许provider和consumer通过RpcContext进行跨端之间的参数传递;
* consumer在使用时，只需要通过RpcContext进行参数设置即可;
```java
import org.season.ymir.core.context.RpcContext;

@RestController
public class TestController {

  @Reference
  private TestService service;

  @PostMapping("/name")
  public String get(@RequestParam("name") String name) {
    RpcContext.getContext().setAttachments("testKey", "testValue");
    return service.test(name);
  }
}
```
* provider就可以通过RpcContext进行获取传递的参数;
```java
import org.season.ymir.core.context.RpcContext;

@Service
public class TestServiceImpl implements TestService {
  @Override
  public String test(String name) {
    RpcContext.getContext().getAttachments().get("testKey");
    return "Hello " + name;
  }
}
```
### 异步调用
#### 如何使用
* Ymir允许客户端异步调用服务端的接口，有以下两种方式开启异步调用；
  * 在[Reference](src/main/java/org/season/ymir/core/annotation/Reference.java) 注解中将`async`设置为`true`即可;
  * 在方法调用前通过隐式传参`RpcContext.getContext().setAttachments("async","true")`即可;
* 设置了异步调用后，客户端调用接口的返回会返回`Null`，客户端需要通过`RpcContext.getFuture()`返回的`CompletableFuture`对象完成后续的操作，其中`CompletableFuture`中的值就是接口定义的返回值；

```java
import org.season.ymir.core.context.RpcContext;

@RestController
public class TestController {

  @Reference(async = true)
  private TestService service;

  @PostMapping("/name")
  public String get(@RequestParam("name") String name) {
    // 此时这个接口会返回Null
    String response = service.test(name);
    CompletableFuture<Object> future = RpcContext.getFuture();
    future.whenComplete(object -> {
      System.out.println("接口调用");
    });
    return response;
  }
}
```
### SPI
#### Java SPI
* Java的SPI允许我们在对应的位置添加实现，就可以通过`ServiceLoader`来加载对应的接口实现，但是缺点在于会一次性加载所有的扩展点，例如：
* 新增一个接口
```java
public interface SpiInterface {
    String hello();
}
```
* 增加两个实现
```java
public class SpiInterfaceImplOne implements SpiInterface{
    @Override
    public String hello() {
        return "哈哈哈";
    }
}

public class SpiInterfaceImplSecond implements SpiInterface{
  @Override
  public String hello() {
    return "嘿嘿嘿";
  }
}
```
* 在resources目录下的META-INF/services/新建文件名org.season.ymir.spi.SpiInterface，填充值
```text
org.season.ymir.spi.SpiInterfaceImplOne
org.season.ymir.spi.SpiInterfaceImplSecond
```
* 运行测试用例
```java
public class SpiTest {
  @Test
  public void testJavaSpi() {
    Iterator<SpiInterface> iterator = ServiceLoader.load(SpiInterface.class).iterator();
    while (iterator.hasNext()) {
      System.out.println(iterator.next().hello());
    }
  }
}
```
* 查看返回结果
```text
哈哈哈
嘿嘿嘿
```
* 可以看到通过`ServiceLoader`去加载实现时，一次性加载了它的所有实现，而Ymir想要的是在需要的时候去加载对应的实现，所以通过Java SPI是不满足的。
#### Ymir SPI
* [Dubbo SPI](https://dubbo.apache.org/zh/docs/v2.7/dev/source/dubbo-spi/) ，在需要的时候去加载对应的实现，避免资源浪费
* [Dubbo SPI](https://dubbo.apache.org/zh/docs/v2.7/dev/source/adaptive-extension/) 在原有的Java SPI基础上扩展了很多，包括自适应加载等非常强大的功能
* Ymir借鉴了Dubbo SPI以及[Shenyu SPI](https://github.com/apache/incubator-shenyu/tree/master/shenyu-spi) 的设计，基本做到了在需要的时候去加载对应实现的功能，通过`ExtensionLoader`来加载实现
* 新增一个接口
```java
// @SPI注解来标注这是一个可以被SPI管理的类，value代表它的默认实现
@SPI("one")
public interface SpiInterface {
    String hello();
}
```
* 增加两个实现
```java
public class SpiInterfaceImplOne implements SpiInterface{
    @Override
    public String hello() {
        return "哈哈哈";
    }
}

public class SpiInterfaceImplSecond implements SpiInterface{
  @Override
  public String hello() {
    return "嘿嘿嘿";
  }
}
```
* 在resources目录下的META-INF/ymir/新建文件名org.season.ymir.spi.SpiInterface，文件名为接口的全路径名；
> 这里的内容为key=value的样式，其中key为对应的扩展实现，通过`ExtensionLoader`进行加载时需要用到
```text
one=org.season.ymir.spi.SpiInterfaceImplOne
two=org.season.ymir.spi.SpiInterfaceImplSecond
```
* 运行测试用例
```java
public class SpiTest {
    @Test
    public void testYmirSpi() {
        // 当getLoader里为空时，会读取@SPI注解内的默认值的实现
        SpiInterface loader = ExtensionLoader.getExtensionLoader(SpiInterface.class).getLoader("");
        System.out.println(loader.hello());

        // 加载key为one的实现类
        SpiInterface loaderOne = ExtensionLoader.getExtensionLoader(SpiInterface.class).getLoader("one");
        System.out.println(loaderOne.hello());

        // 加载key为two的实现类
        SpiInterface loaderTwo = ExtensionLoader.getExtensionLoader(SpiInterface.class).getLoader("two");
        System.out.println(loaderTwo.hello());
    }
}
```
* 查看返回结果
```text
哈哈哈
哈哈哈
嘿嘿嘿
```
* 在Ymir的很多地方都用到了SPI的，例如负载均衡和序列化
### 服务注册
* 服务注册信息会落到本地缓存内，防止注册中心挂掉后，服务端无法处理请求；
  ![](./images/Ymir服务注册.png)
### 服务发现
* 服务发现主要是在客户端操作，客户端来发现自己需要注入的服务信息，之后存储在本地，用来后续的服务调用发起
  ![](./images/Ymir服务发现.png)
### Netty请求处理器
#### 心跳检测处理器

* 基于Netty的[IdleStateHandler](https://github.com/netty/netty/blob/4.1/handler/src/main/java/io/netty/handler/timeout/IdleStateHandler.java)
  * 客户端监听写时间，超时时间为30s；
  * 服务端监听读事件，超时时间为2min；
* 具体做法
  * 客户端监听写事件，如果在30s内，客户端没有写事件发生，触发[IdleStateEvent](https://github.com/netty/netty/blob/4.1/handler/src/main/java/io/netty/handler/timeout/IdleStateEvent.java)
    * 第一次发送心跳请求，客户端心跳请求次数+1，服务端收到心跳请求，做出响应，客户端收到心跳响应，心跳请求次数重新置为0，本次心跳结束，等待下一次心跳；
    * 第一次发送心跳请求，客户端心跳请求次数+1，服务端收到心跳请求，但是未响应心跳结果。客户端未收到心跳响应，等待30s后，继续发送心跳请求，如果心跳请求超过3次后，仍未获取到服务端响应心跳结果，客户端主动关闭通道，断开连接，清除缓存；
  * 服务端监听读事件，如果在2min内，服务端没有读事件，触发[IdleStateEvent](https://github.com/netty/netty/blob/4.1/handler/src/main/java/io/netty/handler/timeout/IdleStateEvent.java)
    * 服务端监听到读事件，删除当前客户端连接地址缓存，服务端关闭连接；
      ![](./images/Ymir心跳检测.png)
#### 编码/解码处理器

* Ymir的编码解码依然用的是Netty自己的编码解码器，在里面对写出以及接收到的数据进行编码解码操作；
  * [MessageToByteEncoder](https://github.com/netty/netty/blob/4.1/codec/src/main/java/io/netty/handler/codec/MessageToByteEncoder.java)
    编码器
  * [LengthFieldBasedFrameDecoder](https://github.com/netty/netty/blob/4.1/codec/src/main/java/io/netty/handler/codec/LengthFieldBasedFrameDecoder.java)
    解码器
* 在客户端和服务端都添加了相同的编码器和解码器
  * [MessageEncoder](src/main/java/org/season/ymir/core/codec/MessageEncoder.java) 编码器
  * [MessageDecoder](src/main/java/org/season/ymir/core/codec/MessageDecoder.java) 请求解码器
### 序列化
#### Gson序列化
* 使用GsonUtils工具类来对请求参数以及返回参数进行序列化，反序列化操作
* 需要在配置文件中设置序列化方式为Gson序列化
#### Protostuff序列化
* 采用[Protostuff](https://github.com/protostuff/protostuff) 的序列化方式，是Ymir默认的序列化方式
#### Kryo序列化
* 采用[Kryo](https://github.com/EsotericSoftware/kryo) 的序列化方式
#### 序列化性能对比
  ![](./images/Ymir序列化.jpg)
### 负载均衡
#### 随机
* 随机获取存在的服务列表中的某一个Service，计算时加权重
#### 轮询
* 轮询请求服务列表中的Service

## 有问题反馈
在使用中有任何问题，欢迎反馈给我，可以用以下联系方式跟我交流
* Email: kevinclair@apache.org

## 致谢
感谢以下的项目，在个人学习过程中，给我起到了很大的帮助
* [Dubbo](http://dubbo.apache.org/zh-cn/)
* [shenyu](https://github.com/apache/incubator-shenyu)

## 最近计划
最近准备开始着手做一次项目的重构和升级，调整一下项目的模块以及工程架构；
* @Service属性扩展，增加多版本，分组等属性；
* @Reference属性扩展，多版本，分组等；
* 负载均衡算法优化；
* 增加provider和consumer的线程池配置参数;
