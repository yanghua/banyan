#overview
消息总线的完整功能离不开围绕在MQ周围的一些核心服务。这些服务作为长时间任务，宿主在某一服务器上。 `server` Module就定义以及处理了这些逻辑。

下图展示了server端程序的模块图：
![img 1][1]

- app : 为server的入口，提供了server的启动逻辑；
- bootstrap : 为server最先启动的核心服务，通常包括了比如：rabbitmq-server的启动/zookeeper的启动；它们是同步启动；并且不允许失败的
- daemon : package内部定义了系统其他的后台服务

##关于bootstrap
它定义了系统核心组件的启动逻辑，是消息总线可靠运行以及其他一切的前提。它们必须顺序、同步启动；如果没有这个前提，后续所有的东西都不会发生。
目前实现了两个单例的initializer：

* RabbitmqInitializer 
* ZookeeperInitializer

##关于daemon
它的设计图如下：
![img 2][2]


首先，所有的这些后台服务，都实现了IService接口（同时也都实现了Runnable接口，即预示着他们都是以独立线程的方式来运行）。其次，这里通过Java annotation（DaemonService）的方式来实现在运行时对服务进行动态加载以及“自查找”。
这里为服务区分了不同的运行策略（以RunPolicy枚举来实现）：

* ONCE : 只允许一次的
* CYCLE_SCHEDULED :周期性循环执行的

服务实例的创建、加载运行是通过图中 `ServiceLoader` 类来实现的。它会先扫描所有带有 `DaemonService` 的类，并按照 `RunPolicy` 进行分组实例化。这里分组的原因是因为不同运行策略的服务，需要采用不同的线程池来执行；

> 对于策略为只运行一次的，将用Java 中的：`ExecutorService` 的实例来执行；而对于周期性循环执行的，将采用 `ScheduledExecutorService` 的实例来执行，他们都维护着不同类型的线程池。

###AuthorizeService
身份认证的服务，它是 `ONCE` 服务，虽然它只运行一次，但它是永久运行的（全生命周期）。
实现：它可以是一个http的代理服务

###UpstreamSysMsgService
上行系统消息服务，它是 `ONCE` 服务，它也是全生命周期的。实现：在内部以while(true)的方式侦听名为 ***queue.proxy.message.sys.XXX*** 的队列。

###SystemMonitorService
系统资源监控服务，它以 `CYCLE_SCHEDULED` 方式运行，它以某个设定的周期定时执行。

###MsgLogService
消息总线中的消息Log服务，它以 `ONCE` 方式运行，内部同样以while(true)方式侦听名为 ***queue.proxy.log.file/console*** 的队列，并以文件的方式，记录所有的消息，以供进行异常/错误定位。

##关于系统消息与事件
我认为系统级别的信息有这几种：
* 下行配置
* 上行系统消息
* 下行系统消息  

对于这几种系统级信息的处理可以采用不同的方式：

* 下行配置 ： 走zookeeper主动推送；
* 上行系统消息 ：由客户端发往名为（queue.proxy.message.sys.#）的队列，交由 `UpstreamSysMsgService`处理
* 下行系统消息 ： 走zookeeper做event推送

zookeeper的布局形如：

![img 3][3]

这么做的几种目的：

- 大大减轻rabbitmq本身的负载，无论是channel与queue都大大减少；这样只需要server的`UpstreamSysMsgService` listen queue.proxy.message.sys下的queue即可，每个客户端无需通过listen queue接受系统信息
- 理论上来说，只要是想获得即时消息，就必须开一个socket长连接，这种模式，将rabbitmq的server负载转移到zookeeper的宿主server上去
- 考虑消息总线的系统消息场景，不管是上行、还是下行几乎都是单向，很多系统消息都不是request-response这种模型。级别是这种模型，上下行消息也只不过是走了异步模式的request-response，并且这些消息如果是走queue传输的话也是异步处理的模式
- queue只关注业务系统的消息传递



[1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/server/server-module.png
[2]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/server/daemon-service-design.png
[3]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/server/zoo-keeper-sys-message.png