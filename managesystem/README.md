#overview
消息总线管理系统用于完成对消息总线的管理、监控、参数配置等相关功能。消息总线管理系统自身可以看作是messagebus client的使用者，它操作数据库之后，直接向 `server`发送更新指令。由`server` dump数据库的相关信息，并发送到zookeeper 配置管理中心，zookeeper接收到新的配置后，会将新数据push到所有已连接的客户端。
另外，因为消息总线管理系统作为可视化的管理控制台，它监控的目标也包括了 `server`，因此他们会利用如下两个队列互相通信：

![img 1][1]

因此 `managesystem`跟 `server` 都是 `client` 的使用者。它们可以看作是两个demo。

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
[1_1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/managesystem/maintain_topology.png
[1_2]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/managesystem/maintain_node.png
[5_1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/managesystem/permission_module.png