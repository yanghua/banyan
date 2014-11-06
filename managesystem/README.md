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