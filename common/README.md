#overview

common组件提供了一些公共数据结构的定义（比如消息头/消息体），以及其他module都会用到的数据结构或公共方法。

##message
跟消息有关的数据结构位于message package中：
![img 1][1]

借鉴JMS的设计方式，基本的消息结构设计为接口（这可以避免Java中不允许多继承的局限）。如图，消息头被抽象为 `IMessageHeader`，消息体被抽象为 `IMessageBody`。`IMessageHeader` 提供绝大部分属性的getter/setter访问器（特定消息的部分属性是固定的，它们不允许修改，因此这些属性的setter对外隐藏，由工厂方法或子类直接在内部设置）。message header + body 组成 `Message` 。
消息类型的定义，通过枚举实现在 `MessageType` 中，目前实现的消息类型有：

- QueueMessage : 队列消息
- PubSubMessage : 发布/订阅消息
- BroadcastMessage : 广播消息
- ExceptionMessage : 异常消息

另外提供一个 `MessageFactory` 来解耦消息的创建于实现。
消息设计部分的类图：
![img 2][2]

它以一个抽象实现： `AbstractMessage` 作为所有具体消息的模板（提供了消息头通用实现 `GenericMessageHeader`的初始化）。


[1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/common/message-design.png