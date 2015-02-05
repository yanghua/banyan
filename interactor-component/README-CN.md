#overview

the module encapsulated the interaction with RabbitMW (used rabbitmq-java-client). and stop the direct dependency of rabbitmq-java-client

now it contains three parts:

* rabbitmq : encapsoluted rabbitmq中exchange/queue/channel/topology operate interface
* proxy : provided produce and consume message's api
* message : provided message header / body 's adapter and box and unbox message

##message
这里应对于message header / body的结构，分别为它们提供了processor，如下图：

![img 1][1]

它们都包含两个对外公开的方法：

- box : 用于实现消息头、消息体到rabbitmq-java-client的 ***装箱*** 功能
- unbox : 用于实现rabbitmq-java-client接收到的消息 ***拆箱*** 装入消息头/体的Java对象的功能

> 由于参照物选择不同，或理解不一，这里选择的参照物是 `rabbitmq的消息`

消息头跟消息体的结构特征不同（消息头的格式总是一致，而消息体的格式不尽相同），它们的实现方式也不同。对 `消息头` 而言，由于格式唯一，所有只需要一个处理器；对 `消息体` 而言，由于消息类型众多，所有采用基于接口的实现方式；辅助以工厂类来创建消息体处理器。消息体处理器类图如下：

![img 2][2]

更多关于消息头跟消息体的结构定义信息，请参考：[common 组件](https://github.com/yanghua/messagebus/tree/master/common) 的相关说明

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



[1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/interactor/message-processor.png
[2]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/interactor/message-body-processor.png
[3]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/interactor/proxy-produce.png
[4]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/interactor/proxy-consume.png
