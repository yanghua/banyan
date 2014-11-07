#overview
消息总线的完整功能离不开围绕在MQ周围的一些核心服务。这些服务作为长时间任务，宿主在某一服务器上。 `server` Module就定义以及处理了这些逻辑。server本身也是 `client` 的使用者，默认情况下它对应于 `proxy.message.system.server` 队列。

`server` 包含三大部分：

- app : 为server的入口，提供了server的启动逻辑；
- bootstrap : 为server最先启动的核心服务，通常包括了比如：rabbitmq-server的启动/zookeeper的启动；它们是同步启动；并且不允许失败的
- daemon : package内部定义了系统其他的后台服务

##bootstrap-service
它定义了系统核心组件的启动逻辑，是消息总线可靠运行以及其他一切的前提。它们必须顺序、同步启动；如果没有这个前提，后续所有的东西都不会发生。
目前实现了两个单例的initializer：

* RabbitmqInitializer 
* ZookeeperInitializer

##daemon-service
它的设计图如下：

![img 2][2]

首先，所有的这些后台服务，都实现了IService接口（同时也都实现了Runnable接口，即预示着他们都是以独立线程的方式来运行）。其次，这里通过Java annotation（DaemonService）的方式来实现在运行时对服务进行动态加载以及“自查找”。
这里为服务区分了不同的运行策略（以RunPolicy枚举来实现）：

* ONCE : 只运行一次
* CYCLE_SCHEDULED :周期性循环执行的

服务实例的创建、加载运行是通过图中 `ServiceLoader` 类来实现的。它会先扫描所有带有 `DaemonService` 的类，并按照 `RunPolicy` 进行分组实例化。这里分组的原因是因为不同运行策略的服务，需要采用不同的线程池来执行；

> 对于策略为只运行一次的，将用Java 中的：`ExecutorService` 的实例来执行；而对于周期性循环执行的，将采用 `ScheduledExecutorService` 的实例来执行，他们都维护着不同类型的线程池。

###CommandService
命令服务，它是 `ONCE` 服务，虽然它只运行一次，但它是永久运行的（全生命周期）。它可以响应第三方发来的一些 **命令** 目前这些命令有：

* PING : 第三方发来的确认在线命令，以 **response** 响应 `PONG`
* INSERT : 收到该命令以及一个表名参数，dump该表的数据到本地，然后将数据设置到zookeeper，zookeeper收到之后，会push到所有连接的客户端。
* UPDATE : 同上
* REMOVE : 同上




###UpstreamSysMsgService
上行系统消息服务，它是 `ONCE` 服务，它也是全生命周期的。实现：在内部以while(true)的方式侦听名为 ***queue.proxy.message.sys.XXX*** 的队列。

###SystemMonitorService
系统资源监控服务，它以 `CYCLE_SCHEDULED` 方式运行，它以某个设定的周期定时执行。

###MsgLogService
消息总线中的消息Log服务，它以 `ONCE` 方式运行，它consume名为 ***queue.proxy.log.file/*** 的队列，并以文件的方式，记录所有的消息，以供进行异常/错误定位。

##关于系统消息与事件
系统级别的信息有这几种：
* 下行配置
* 上行系统消息
* 下行系统消息  

对于这几种系统级信息的处理可以采用不同的方式：

* 下行配置 ： 走zookeeper主动推送；
* 上行系统消息 ：由客户端发往名为（queue.proxy.message.system.#）的队列，交由 `UpstreamSysMsgService`处理
* 下行系统消息 ： 走zookeeper做event推送

zookeeper的布局形如：

![img 3][3]


[1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/server/server-module.png
[2]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/server/daemon-service-design.png
[3]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/server/zookeeper-structure.png