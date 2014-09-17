#overview

common组件提供了一些公共数据结构的定义（比如消息头/消息体），以及其他module都会用到的数据结构或公共方法。

##message
跟消息有关的数据结构位于message package中：
![img 1][1]

借鉴JMS的设计方式，基本的消息结构设计为接口（这可以避免Java中不允许多继承的局限）。如图，消息头被抽象为 `IMessageHeader`，消息体被抽象为 `IMessageBody`。它们提供绝大部分属性的getter/setter访问器（特定消息的部分属性是固定的，它们不允许修改，因此这些属性的setter对外隐藏，由工厂方法或子类直接在内部设置）。message header + body 组成 `Message` 。由于消息头的属性对所有消息而言是一致的，所以只提供一个通用实现 `GenericMessageHeader`，而由于各消息的消息体不尽相同，因此采用接口继承的方式来实现多态。
消息类型的定义，通过枚举实现在 `MessageType` 中，现有实现的消息类型有：

- AppMessage : **应用程序** 消息
- AuthreqMessage : **授权/认证请求** 消息
- AuthrespMessage : **授权/认证响应** 消息
- LookupreqMessage : **查询路由信息请求** 消息
- LookuprespMessage : **查询路由信息响应** 消息
- CacheExpiredMessage : **缓存失效系统** 消息

另外提供一个 `MessageFactory` 来解耦消息的创建于实现。
消息体的实现类图：
![img 2][2]




[1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/common/message-design.png
[2]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/common/messagebody.png