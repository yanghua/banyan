#overview

本项目用于对消息总线的 `client` 进行测试，以展示不同场景下 `client` 的性能表现。

##测试说明

配置说明：

client : 

```
OS : Mac os x Yosemite (version 10.10)
Processor : 2.5GHz Intel Core i5
Memory : 8GB 1600 MHz DDR3
JDK Version : 1.7.0_45
```

server :

```
OS : Ubuntu Server 14.04.1 (GNU/Linux 3.13.0-37-generic x86_64)
Processor : Intel(R) Xeon(R) CPU E3-1230 V2 @ 3.30GHz (8核)
Memory : 8GB
JDK Version : 1.7.0_72
```

> 每次测试之前，rabbitmq都会重新初始化，也就是说，每次测试之前总线中没有消息！

##实现说明
测试模块的相关类图：

![img 1][1]


测试逻辑由一个个测试用例构成，测试用例都是形如XXXTestCase的Java文件，所有的TestCase都继承自 `Benchmark` 类，它提供了一个 `test` 方法：

```
public void test(Runnable testTask, int holdTime, int fetchNum, String fileName);
```
它需要的几个参数：

* testTask - 具体的测试逻辑所属类，上面的 `BasicProduce`的实例
* holdTime - 测试维持的毫秒数
* fetchNum - 采样次数
* fileName - 采集数据要存储的文件名

`test` 方法提供了一个实现，并完成了一些 `Aspect`，它们有：

* 日志记录
* testTask的多线程启动
* 测试数据的采集
* testTask的关闭
* 采集数据写入文件


每个继承自 `Benchmark` 的 testcase 都包含有若干个 testTask (它们都是静态内部类，都实现了 `Runnable`接口，如上图的 `BasicProduce`)，每个testcase都包含有一个 `main` 入口方法，该方法用于运行测试。
对于每一个testTask，它们通常都会实现两个接口：

* ITerminater : 终止testTask的接口
* IFetcher : 提取/采集测试数据的接口

这两个接口在 `Benchmark` 的 `test` 方法中使用。

除此之外，还有几个辅助类：

- TestMessageFactory : 用于安装给定的size来生成测试消息
- TestConfigConstant : 用于统一配置一些测试常量
- TestUtility : 帮助类，测试数据写文件等


##scenario
###produce

> orignal : native-java-client 发送，但也走server的topology route，测试时共享一个channel
> client : 如无特殊说明，client发送时都基于pool-channel模式

* 单线程，不同大小的消息体，循环发送，对比：

![img 2][2]

* 单线程，相同大小的消息体，是否使用client channel pool，对比：

![img 3][3]

[1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/benchmark/benchmark-class-diagram.png
[2]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/benchmark/produce/singleThreadClientVSOriginal.png
[3]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/benchmark/produce/singleThreadClientVSOriginal.png