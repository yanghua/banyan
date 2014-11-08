#overview
消息总线管理系统用于完成对消息总线的管理、监控、参数配置等相关功能。消息总线管理系统自身可以看作是messagebus `client` 的使用者，它操作数据库之后，直接向 `server`发送更新指令。由`server` dump数据库的相关信息，并发送到zookeeper 配置管理中心，zookeeper接收到新的配置后，会将新数据push到所有已连接的客户端。

##sentinel
另外，因为消息总线管理系统作为可视化的管理控制台，它监控的目标也包括了 `server`，因此他们会利用如下两个队列互相通信：

![img 1][1]

 `web`跟 `server` 都是 `client` 的使用者。 `web` 是请求者，它另起一个线程向 `server` 定期（目前暂定10秒钟） **request** ***PING*** , `server` 上有个独立的服务 `Sentinel` daemon service，专门用于 **response** ***PONG***, 如果 `web`在预设的超时时间内(目前暂定10秒钟)，没有收到 `server`的响应，则认为 `server` 的状态异常。
 
 ![img 2][2]
 
 示意图如上所示，它们通信的模型基于 **request/response**

##技术说明

* 数据存储：mysql
* Ioc: Spring
* 表现层框架：Struts 2
* 持久层框架：mybatis
* 前端组件：jTable,jQuery,layer


##功能模块

* 信息维护
* 监控管理
* 预警管理
* 配置管理
* 权限管理

###信息维护

节点是rabbitmq中的exchange与queue的抽象，该模块除了对节点的维护（增删查改）外，还包含对节点的管控，这里的管控分为两个方面：

###预警管理
预警管理模块用于完成对所有队列的更“细粒度”的设置以及预警阈值设置，这些设置部分是针对服务端监控，部分是下达给客户端组件内部。

- 激活/禁用
- 发送授权
- 接收授权
- 消息体大小控制
- 队列滞留消息数阈值控制
- 消息发送的流速控制

####激活/禁用
这是队列开关，队列正常情况下处于激活状态。一旦被设置为禁用，则该队列或者说其对应的服务将处于不可用状态

####发送授权
一个队列要想与别的队列交互，单处于激活状态下是不够的。就它能往哪个队列发送消息，还取决于它是否有往目标队列的发送权限，这就是发送授权的功能

####接收授权:
同上，一个队列并不是连接到哪个队列，就可以接收哪个队列的消息，这还取决于接收授权是否授予其接收该队列消息的权限

####消息体大小控制:
如果对该阈值项进行设置，那么客户端在发送或接收消息，会检查消息体大小

####队列滞留消息数阈值控制：
为了防止某个队列的滞留消息数太多，而影响整个消息总线的内存空间、性能，可以设置队列的滞留消息数阈值，在达到阈值时，可以触发相应的处理策略：邮件报警、将队列置为禁用、消息转移存储...

####消息发送的流速控制:
出于安全目的，或保护消息总线的目的，可以控制client发送消息的速率，一旦高于设置的阈值，可认为其是恶意发送，可以对其采用相应的策略，比如：禁用、报警等

>以上这些设置，都可以通过zookeeper实时push到所有正在工作的client，并立即生效。


##功能界面

* 信息维护

![img 1_1][1_1]

![img 1_2][1_2]

* 权限管理

![img 5_1][5_1]


[1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/managesystem/web-server-queue.png
[2]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/managesystem/web-server-sentinel.png
[1_1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/managesystem/maintain_topology.png
[1_2]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/managesystem/maintain_node.png
[5_1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/managesystem/permission_module.png