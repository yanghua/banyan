#overview
消息总线项目各Module（子项目）交互关系图：

![img 1][1]

消息总线项目各Module（子项目）依赖关系图：
![img 2][2]

其中，两个为组件：

* common : 通用底层组件，定义了一些基本的数据结构，如果消息、消息头、消息体等
* interactor-component : 交互组件，用于解耦其他各Module跟rabbitmq的依赖

另外的三个为项目：

- client : 消息总线的客户端，提供给其他应用访问消息总线的接口
- server : 服务端，包含了元数据管理，系统监控，日志记录等，一个daemon server
- managesystem : web管理系统，主要用于配合 **server** 进行一些可视化的监控以及提供一些管理功能

> managesystem 与 server共享数据库。主要是因为server的部分功能跟managesystem互为补充，它们通过数据库交互。比如通过managesystem的管理界面设置预警阈值，但真正的处理、判断逻辑还是由server来完成。但server是消息总线的必备组成部分，而managesystem是可选的。

##核心交互组件
由于除common不需要跟rabbitmq交互之外，以上其他的module都有跟rabbitmq交互的需求。因此从降低依赖的角度出发，我们有必要通过 ***组件化*** 的手段，来达到封装变化的目的。在消息总线中，所有跟rabbitmq交互的操作都被封装在 `interactor-component` 中。这样后面任何关于rabbitmq-java-client的变化，都只需修改该组件。








[1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/overview/architecture.png
[2]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/overview/module-dependency.png