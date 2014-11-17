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
* 多样的消息通信模式：点对点、发布/订阅、广播
* 在不干涉消息传输链路的情况下，实现消息日志
* all-in-one 一键配置，主动push

##Module关系

![img 1][1]

> 此图中连线的箭头，只表示访问的依赖关系，不表示数据的走向！

消息总线项目各Module（子项目）依赖关系图：
![img 2][2]

> 此图根据maven的module dependency生成（它除了分析了直接依赖关系，同时还分析了间接依赖关系）。 `httpbridge`、`server`、`managesystem`都是 `client` 的使用者，因此它们都直接依赖 `client`（不直接依赖 `interactor-component`）。因此， 这里具有层次化的抽象关系：`interactor-component` 抽象了跟Message Broker最原始的交互，client使用 `interactor-component` 实现与Message Broker的交互，同时加入其他控制逻辑，形参了对Message Bus交互的抽象层。`server`、`managesystem`需要跟Message Bus进行交互，它们只能使用client；而 `httpbridge` 本身就是 `client` 的Proxy用于提供http形式的访问。 这也是上面提到的，它们三者都是 `client` 的client。

其中，三个为组件（前两个是业务无关的组件，第三个是业务组件）：

* common-component : 通用组件，提供了公共的utility、helper工具类。处于它之上的每个高层Module，都可以直接依赖它。（它是最稳定的）
* interactor-component : 交互组件，用于解耦其他各Module跟Message Broker(rabbitmq)、ZooKeeper的依赖，抽象它的意义在于，后面升级这些开源组件的版本时，对其进行的适配与更改动作，对 `client` 透明（它只能被client依赖）
* business-component : 定义了一些基本的公共数据结构，如果消息、消息头、消息体、节点、配置等，它与业务逻辑、实现逻辑有关

另外的四个Module：

- client : 消息总线的客户端，提供给其他应用访问消息总线的接口
- server : 服务端，包含了rabbitmq管理，系统监控，日志记录等，一个daemon server
- managesystem : web管理系统，主要用于配合 **server** 进行一些可视化的监控、提供管理功能
- httpbridge : 消息总线的http访问接口，用于实现异构系统环境中（非java语言环境）访问消息总线，可以将其看成client的代理

> managesystem 与 server共享数据库。主要是因为server的部分功能跟managesystem互为补充，但它们并不直接交互，它们都是 `client`的client，它们之间通过Message Bus交互。但server是消息总线的必备组成部分，而managesystem是可选的。


##实践说明
* 队列职责单一化，一个队列处理器**尽量**只处理**一种**类型的**业务**消息
* 拓扑图最后一层节点始终是队列节点，而非exchange节点
* 构建队列之前，权衡好粒度
* 以面向服务或面向组件的开发方式来分拆服务提高复用性与可靠性

##Todo-List
* ~~server实现路由拓扑结构的一键初始化~~
* ~~server中通过命令控制rabbitmq启动~~
* ~~将server自身的启动做成unix-daemon-server的启动方式，编写安装、部署说明文档~~
* ~~messagebus-server 启动、关闭，等系统事件通过zookeeper push到客户端~~
* 对client进行优化，考虑线程安全问题（同步）
* ~~考虑将zookeeper的部分操作封装到 `interactor-component`~~
* 在各Module中加入异常收集功能，抛出的异常，自动回发到 `queue.proxy.message.sys.exception-collector` 中（进行中）
* client加入更多的filter，配合动态配置，实现对client更细化的控制
* ~~实现授权/认证检查(appId 对应的授权发送队列与接收队列)~~
* ~~考虑加入Pub/Sub的实现~~
* ~~client/httpbridge性能测试的量化数据~~
* web管控台，注册节点后，向server发送：更新命令（server将数据库的更新数据主动push到客户端）
* 将各个module内的常量定义配置到数据库
* 将zookeeper推送到客户端的配置信息加密
* ~~重新整理 `server` 端的配置、让 `server` 直接依赖 `client` ~~
* 优化从数据库dump并设置到zookeeper中要推送给客户端的数据（目前是采用mysqldump导出，冗余信息较多），这也可以将部分数据改用redis来存储，然后借助于其 pub/sub来进行优化
* 处理在windows系统下，从zookeeper内dump出来的数据存储问题（系统的公共位置）
* 将zookeeper 推送的内容从原始的数据库xml数据表示，修改为java序列化对象
* 将很多横向处理的变化点(很多switch)，修改为继承的可扩展模式



[1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/overview/architecture.png
[2]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/overview/module-dependency.png
[3]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/overview/router-topology.png