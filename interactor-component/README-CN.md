#overview

该模块是消息总线的“技术组件”，用于封装第三方组件以避免直接依赖。

* rabbitmq：封装了rabbitmq的一些管理接口
* proxy：提供了produce/consume的核心API
* pubsub：发布/订阅的实现


##proxy
proxy package提供了message produce / comsume的代理。当然它们的实现都相对 **primitive** ，更多的功能、特性可以由第三方包装实现，当然后续它们的接口可能还要重做修改。

produce:

![img 3][3]

consume:

![img 4][4]

关于consume，提供了两个同名的方法 `consume`，说明如下：

- instance-method : 见图中 `run` 方法下面，它是通用目的的消息消费器，接收一个listener作为callback参数
- static-method : 见图中最后一个方法，它返回原生的 `QueueingConsumer`，它提供给第三方自定义，便于扩展

为了通用目的consume（instance-method）的实现（它必须在一个独立的线程上开启一个event-loop），因此 `ProxyConsumer` 也被实现为一个线程的（它直接实现了， `Runnable` 接口），consume只是用于对线程实例的启动进行触发。它在内部获取到 `QueueingConsumer` ，然后调用 调用 `start` 方法进而触发 `run` 的处理逻辑。在 `run`内部实现了一个 `while(true)` 来接收消息、unbox消息头，消息体；load Message 对象，触发listener的callback。

##pubsuber
pubsuber（称为发布订阅器）为应用注册以及实时管控提供了技术上的实现手段。它基于publish/subscribe抽象出了一套接口，任何支持socket长连接的第三方组件，只要基于其实现这套接口都可以成为“推送源”。目前消息总线已支持的推送源有：`redis`以及`zookeeper`。

pubsuber的结构如下图：

![img 5][5]

* IPubSuber: 发布订阅器的对外接口
* PubSuberFactory: 一个创建`IPubSuber`实例的工厂类
* IPubSubListener: 订阅器接收到消息时的事件处理器接口
* IDataConverter: 由于各推送源存储消息的格式不一致，提供一个称之为数据转换器的接口，来提供序列化与反序列化

展开两个impl package的具体实现类图：

![img 6][6]

由于推送源的客户端的生命周期跟消息总线的客户端的生命周期都一样的，因此其取名都一致为：LongLive* （默认情况下，zookeeper的watcher是有session的，失效后便不再重连），`LongLiveZookeeper`是改进后的自动重连的zk-client。

关于数据转换的实现：针对zookeeper，提供了Java对象序列化存储机制；针对redis，提供了json对象转字符串后存储的机制（redis不接收二进制格式为非字符串的数据存储）。

关于Ios，pubsuber对其他组件以及客户端一律面向上面的接口。至于`PubSuberFactory`实例化的是`IPubSuber`的哪个实例，这取决于配置文件（J2SE提供的 `ServiceLoader`实现）配置。


[1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/interactor/message-processor.png
[2]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/interactor/message-body-processor.png
[3]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/interactor/proxy-produce.png
[4]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/interactor/proxy-consume.png
[5]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/interactor/pubsuber-structure-diagram.png
[6]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/interactor/pubsuber-class-design-diagram.png
