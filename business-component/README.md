#overview
`business-component` 是消息总线的业务组件。

##动机
原先消息总线中并没有`business-component`，只有`common-component`与`interactor-component`。但这里存在几个问题：

* `common-component`为核心组件，几乎应该杜绝对它的修改
* `interactor-component`虽然对技术组件提供了封装(这里主要针对zookeeper)，但只是通用性封装，客户端面对的封装后的接口仍然是二进制数据的底层接口
* 原先`common-component`中的一些通用Model对象加上`interactor-component`的技术相关的接口结合起来作用于`server`、`client`，造成强依赖性，牵一发而动全身

现在 `common-component`与`interactor-component`之上（`server`与`client`之下）抽象出一层业务相关的“胶合层”（见《Unix编程艺术》），来专门处理相对变化频繁的“业务”实现。

##结构
目前`business-component`对三个方面给出了定义：

* model : 需要在各个module中使用的变更对象的数据结构
* exchanger : 变更数据的交换器

###Exchanger
交换器的作用是：当`server`端有数据变更，需要把这些数据通过一个推送源(比如zookeeper)推送给目标 `client`，而在它们跟推送源交互的过程中，数据的格式需要进行转换（对象<->二进制），交换器的目的就是让转换的过程以及上传跟下载的数据对应推送源的存取机制对外界透明。

原先，虽然在`interactor-component`中对Zookeeper的访问提供了抽象，但只包括了二进制数据的传入与获取。Exchanger用于提供更高级别的抽象——`Server`、`Client`只负责传入、接收变更的数据对象。至于序列化与反序列化以及跟`interactor-component`的交互这两个module不需要关心。这样做的一个目的是为了封装变化、最小化依赖，另一个目的是解耦合（后续考虑用redis的pubsub来替换zookeeper客户端也无需对其他module任何改动）。

类图：

![img 1][1]

Exchanger从大的层面上来看是一个**观察者**模式的实现（简单来看推送源本身就是个观察者模式的实现）。

其中 `IExchangerListener` 应对于Subject，凡是希望获取变更的目标对象都需要实现这个借口。而`ExchangerManager`则是一个 **代理** Observer，说代理是因为真正的观察者其实是`interactor-component`组件中的Zookeeper，因此它其实只是个Subject的注册中心。而`IDataExchanger`则抽象了数据交换器的行为，它提供了四个接口，来跟“推送源”交互：

* upload
* download
* upload(Serializable)
* download(byte[])

其中，不带参数的两个为自动处理方法，带参数的可以看作为独立使用时的“帮助方法”。各种不同对象的格式转换，都是实现类自己去处理。当推送源的选择变化时，只需重新实现一套exchanger，其他module的编程接口不会发生变化。

这里因为这些数据都是来自于数据库，因此采用一个`@Exchanger`注解来在运行时获取必要的信息，它提供两个属性：

* table : 对应的数据表名
* path : 对应的zookeeper节点名称

`ExchangerManager`会在运行时查找并初始化这些exchanger。从而实现“自发现”的注入功能。

每个exchange都可以自动提交变更数据，这里有一个问题，变更数据从哪里获取？这就是另一个接口： `IDataFetcher`的作用。它定义了一个方法：

* fetchData

用于返回一个 `ArrayList`（ArrayList本身实现了Serializable接口）。每个exchanger都通过`IDataFetcher`的实现来获取要推送的数据源。但你发现接口中并没有包含对其的设置？那是因为对于它们的设置，是通过lazy load的模式在运行时通过反射注入给每个exchanger的（这就是不需要对upload提供数据源，也可以以接口一致的方式批量upload的原因，当然你需要对这些exchanger的fetcher进行配置）。

> 批量upload的场景是存在的(当Server启动时，对Zookeeper进行初始化)；批量download的场景却不存在，因为它们download的数据结构不同，并且针对性的处理也不同。


[1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/business/exchanger-diagrem.png

