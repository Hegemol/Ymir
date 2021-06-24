# Ymir
## 背景
Ymir，出自动漫[进击的巨人](https://baike.baidu.com/item/%E8%BF%9B%E5%87%BB%E7%9A%84%E5%B7%A8%E4%BA%BA/65641?fr=aladdin) 中的角色，始祖巨人尤米尔。因为很喜欢这个动漫，所以以这个命名了这个项目。

## Ymir是什么?
一个Rpc框架的示例项目，大部分思路都来源于[Dubbo](http://dubbo.apache.org)的设计思想，主要的功能是基于Netty的服务之间的调用。

## Ymir有哪些功能？

* 声明式服务暴露；
* 声明式服务引用；

## 有问题反馈
在使用中有任何问题，欢迎反馈给我，可以用以下联系方式跟我交流
* Email: kevinclair@apache.org
* QQ: 704714211

## 感激
感谢以下的项目，在个人学习过程中，给我起到了很大的帮助

* [Dubbo](http://dubbo.apache.org/zh-cn/)
* [shenyu](https://github.com/apache/incubator-shenyu)

## 关于作者
94年萌新一枚，目前还是在持续学习阶段，欢迎大佬们多多指教。

## 最近计划
最近准备开始着手做一次项目的重构和升级，调整一下项目的模块以及工程架构；
* 多序列化实现接入(Kryo,Json等)；
* Netty优化，粘包拆包；
* 取消SpringBoot父框架；
* YmirService属性扩展，增加register,group等属性；
* YmirReference属性扩展，增加group，loadbalance等属性；
* 项目结构优化，包路径重构；
* 第二次测试；
