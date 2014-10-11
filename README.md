#overview
消息总线用于对各种异构系统之间消息的通信提供支持。其常见的应用场景有：

* 系统集成
* 面向组件/模块的分布式开发
* 为ESB/SOA的实现提供底层支持

##树状路由拓扑结构
消息总线内部实现基于rabbitmq，借助于rabbitmq多样的消息 exchange 类型，可以构建出各种功能十分强大的路由模式。下面是消息总线实现的路由拓扑结构：

![img 3][3]

树状路由结构拥有如下优势：

* 对客户端隐藏路由结构（只需知道首节点:proxy即可）
* 多样的消息发送模式：单播 、 多播 、 组播
* 在不干涉消息传输链路的情况下，实现消息日志
* all-in-one 一键配置，主动push

##Module关系

![img 1][1]

> 此图中连线的箭头，只表示访问的依赖关系，不表示数据的走向！

消息总线项目各Module（子项目）依赖关系图：
![img 2][2]

> 此图根据maven的module dependency生成，其实 `httpbridge` 不直接依赖 `interactor-component`，上图的依赖关系是因为 `httpbridge` **只** 直接依赖于 `client`，而 `client` 又依赖于 `interactor-component`而导致的间接依赖关系。

其中，两个为组件：

* common : 通用底层组件，定义了一些基本的数据结构，如果消息、消息头、消息体等
* interactor-component : 交互组件，用于解耦其他各Module跟rabbitmq的依赖

另外的四个为项目：

- client : 消息总线的客户端，提供给其他应用访问消息总线的接口
- server : 服务端，包含了rabbitmq管理，系统监控，日志记录等，一个daemon server
- managesystem : web管理系统，主要用于配合 **server** 进行一些可视化的监控以及提供一些管理功能
- httpbridge : 消息总线的http访问接口，用于实现异构系统环境中（非java语言环境）访问消息总线，可以将其看成client的代理

> managesystem 与 server共享数据库。主要是因为server的部分功能跟managesystem互为补充，它们通过数据库交互。比如通过managesystem的管理界面设置预警阈值，但真正的处理、判断逻辑还是由server来完成。但server是消息总线的必备组成部分，而managesystem是可选的。

##核心交互组件
由于除common不需要跟rabbitmq交互之外，以上其他的module都有跟rabbitmq交互的需求。因此从降低依赖的角度出发，可以通过 ***组件化*** 的手段，来达到封装变化的目的。在消息总线中，所有跟rabbitmq交互的操作都被封装在 `interactor-component` 中。这样后面任何关于rabbitmq-java-client的变化，都只需修改该组件。


##实践说明
* 队列职责单一化，一个队列处理器**尽量**只处理**一种**类型的**业务**消息
* 拓扑图最后一层节点始终是队列节点，而非exchange节点
* 构建队列之前，权衡好粒度
* 以面向服务或面向组件的开发方式来分拆服务提高复用性与可靠性

##Todo-List
* server实现路由拓扑结构的一键初始化
* server中通过命令控制rabbitmq启动
* 将server自身的启动做成unix-daemon-server的启动方式
* 对client进行优化，考虑线程安全问题（同步）
* 考虑将zookeeper的部分操作封装到 `interactor-component`
* 在各Module中加入异常收集功能，抛出的异常，自动回发到 `queue.proxy.message.sys.exception-collector` 中
* client加入更多的filter，配合动态配置，实现对client更细化的控制
* 实现授权/认证检查
* client/httpbridge性能测试的量化数据



[1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/overview/architecture.png
[2]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/overview/module-dependency.png
[3]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/overview/router-topology.png