##消息通信

###消息通信模型：
消息通信被实现为三种模型：

* produce/consume
* publish(broadcast)/subscribe
* request/response

###消息通信客户端类
消息通信只能通过唯一的类：`Messagebus` 的实例，它的类图：


从图中可以看出，它继承自一个InnerClient类，并实现了carry包的接口，如下图：

![img 1][1]

从上面两幅图可见，`Messagebus`通过继承以及接口扩展的方式来实现：

![img 3][3]


* 继承：`InnerClient`主要提供了消息通信的基础设施构建
* 扩展：这些消息传输接口的实现

虽然`Messagebus`是消息通信唯一的入口，但它并不承担具体的实现，它只是个代理，每个接口都有真正的实现者。

消息传输被抽象为多种接口：

* IProducer: 定义了生产消息的接口
* IConsumer: 定义了消费消息的接口
* IRequestor: 定义了发送请求消息的接口
* IResponser: 定义了发送响应/应答消息的接口
* IPublisher: 定义了发布消息的接口
* ISubscriber: 定义了订阅消息的接口
* IBroadcaster: 定义了广播消息的接口
* AbstractMessageCarryer: 抽象了消息传输的共性部分（主要包含handler-chain的实例化）

继承关系图如下：

![img 2][2]

> 消息的传输（`produce` `consume` `request` `response` `publish` `subscribe` `broadcast` ）被抽象为carry.


##消息的链式处理
消息的处理利用Pipeline-Filter模式见[POSA卷4]，有利于切割处理模块，方便拆分功能等。我认为这种方式应该是以数据为处理核心的业务模型的首选。
这部分的继承关系图如下：
![img 4][4]

AbstractHandler处于继承类的顶端，为一个抽象的处理器，它定义了三个方法：

* init: 实例化方法，在handler从配置文件中读取并初始化的时候被调用
* handle: 每个继承它的handler所必须实现的 **抽象** 方法，是实现handler-chain的关键
* destroy: 释放资源的触发方法，将在“关闭”messagebus client的时候被逐一调用

上面的各个package封装了每种carry需要用到的handler，它们的使用顺序将会体现在配置文件中。

将处理逻辑以handler进行切分，不仅有利于业务隔离，同时也有利于通过组合进行重用。

其中对于validator，它有共性验证的部分，因此可以抽象出一个基类来实现：

![img 10][10]

当然，他们的先后顺序并不是定死的，而是依赖于配置，配置文件即为该[handler.xml](https://github.com/yanghua/messagebus/blob/master/client/src/main/resources/handler.xml) 文件
![img 6][6]

前面提到的AbstractHandler以及所有的子handler都是用于承载业务实现的，但要串联起它们，就需要另一个接口：

* IHandlerChain: 定义了一个handle方法，用于实现handler-chain
* MessageCarryHandlerChain: 实现了 ***IHandlerChain*** ,并构建了一个用于消息传输的处理器链

##req/resp
req/resp是用于模拟请求/应答的消息交互模型，这也是大部分基于tcp协议的交互方式。但这种模型也是比较常见的，比如有一个队列专门用于授权服务。那么它就是一个比较常见的阻塞式的请求/应答模型。这种模型如果不封装，用通常的produce/consume模型比较难于实现。因为消息总线的消息传递方式默认是 ***异步*** ***不耦合*** 的。下面给出了这种模式的实现方式：

![img 15][15]

通常会认为这种模式，通过p/c listen的队列，中转一下即可，也就是路线1 与 路线2-*。但最终发现，无论找路由拓扑中的某个节点，都会破坏队列的 `职责单一性` ，如果一个队列接收多种不同的消息，而且是混杂式的并且是不同的消费场景，将会使消费端变得混乱、不可控。所以最终的做法是，在produce发往queue-1的时候，以其发送的消息ID，创建一个挂载在rabbitmq上的一个默认的exchange上得临时队列。客户端收到以后，往该临时队列中发送响应信息，producer接收到响应信息后，将临时队列删除。所以正确的路线应为：路线1 与 路线3


##分布式消息Id
消息总线中，消息的生产者是分布式的。在这里的消息Id的生成，借助于Twitter开源的分布式消息ID生成方案。
> 它基于 `Snowflake` 算法，据测试提供的数据：snowflake每秒可以产生26万左右的ID。它的ID基于一定的规则生成：41位毫秒级时间戳+5位datacenter标识+5为节点标识+12位毫秒内序列，另外在加首位的标识符，共64位。
> 用这种算法的好处是，它既能生成唯一的串，同时也是整体在时间上自增排序的，并且在整个分布式系统内不会产生ID碰撞


##Pub/Sub&Broadcast

`pub/sub`，`broadcast`是实现分布式事件以及协同组件工作的有效方式！
借用路由拓扑图的一个局部截图来展示 `pub/sub`，`broadcast`两种消息传输方式实现的机制：

![img 16][16]

如上图所示，我们通过在message节点下挂载一个类型为fanout的exchange节点：`pubsub`。并在其下面再挂载跟business节点下相同的业务/组件queue节点。这样整个结构就变得清晰起来：

* business 对应的消息队列跟发送方具有point-to-point的一对一关系，在JMS中对应的术语是 **queue**
* pubsub 对应的消息队列跟发送方具有一对多的关系，在JMS中对应的术语是 **topic**

> business下的每个队列接收的消息格式必须单一，而pub/sub模式可能有前置条件等多个因素，才导致不采用 **在topic上模拟fanout，并复用队列** 的做法。

可以说 `pub/sub` 是 `broadcast`的特例，因此它们在实现方式上也是一种模式的变种

###broadcast
`broadcast` 很简单，它利用rabbitmq fanout 类型的exchange会自动达到 `broadcast`的效果。通过调用客户端 `broadcast` API，其在内部将消息路由到 `pubsub` 节点，后面的事情就交给rabbitmq，它会将该消息发布到所有挂载在其上的队列里去（同business类似，其下的所有队列都是预先初始化的，客户端没有动态初始化的权限）。为了表明它是一次 `broadcast`，需要对消息的message header中如下的key进行明确的设置：

* type: broadcast
* replyTo: 发送方的queue name
* appId: 发送方的app id
* expiration: （option）如果发送的是事件消息，可能具有时效性，需要设置一个过期实现，按需设置

###pub/sub
`pub/sub` 跟 `broadcast`十分类似，区别只是对message header的设置不同以及对消息的处理方式不同。在一个组件publish一条消息时，需要对message header中的如下key进行明确设置：

* type: pubsub
* replyTo: 发送方的queue name
* appId: 发送方的app id
* expiration: （option）如果发送的是事件消息，可能具有时效性，需要设置一个过期时间，按需设置

由于`pub/sub`类型为fanout，因此进入该exchange的消息，会被其下所有的队列照单全收，而sub的队列可能只是其中的某一个或某几个。因此客户端需要有个filter，记录其所subscribe的所有队列的名称，然后在接到所有消息时，按上面的列出的四个key-value pairs组合过滤出真正应该接受的信息，丢给应用。

> 虽然pub/sub是实现分布式事件以及协同的有效手段，但这里的用法主要还是集中在业务上。系统级别的事件不会走这种模式，仍然走Zookeeper推送，这带来的好处之一是单点失效。如果，rabbitmq或server组件出现问题，它们建立在其上的一切都将失效，所以系统级别的事件还是独立出去。

##权限认证
由于信息的敏感性，并不是一个 `client` 连接上了消息总线，它就可以任意连到任何一个它已知的队列，去“窃取”信息。所有点对点的通信模式（produce/consume、request/response）都需要经过权限认证（这里的权限指的是本队列针对目标队列的收发权限）。此处的权限认证，在客户端实现，不需要连接远程服务器。权限通过 `managesystem` 的授权模块进行授权，这些授权信息对应到数据库中就是many-to-many的关系表。通过 `server` dump出来，然后经由zookeeper推送到所有客户端。客户端拿到数据，解析成Java对象存储于内存中。
###哈希与数组
授权步骤的性能对 `client` 而言非常重要。就拿非批量发送接口（produce）而言，每一次发送（每条消息）都必须经历一次授权的过程，因此我们必须能够快速判断一个队列是否有跟另一个队列交互的权限。常用的访问较快的数据结构有 **哈希**，**数组**。我们知道上面的many-to-many是数据库关系表的模式，而映射到程序中存储，通常演变为one-to-many的关系。哈希这种结构非常适合one-to-one这种形式，而one-to-many的方式对于哈希的value需要引入集合来存储。而这里显然对这个集合也要能够做到快速访问（O(1)），因此能够实现的数据结构还是以上两种，如果选择哈希：此处只为验证key，值域几乎没用，还浪费内存空间；如果选择数组，只能依赖元素都为数字，正好采用索引访问（此处正好满足条件，因为dump下来的数据其实就是数据库表的自增主键）。
###时间与空间
既然这里二级索引考虑采用数组，那么最合适存储大量开关字段的当然是bit数组。在构建这个数组的时候，只需要统计所有被授权编号中最大的作为创建数组的容量，然后遍历所有的索引，如果这个索引存在于授权编号集合中，则置为1否则置为0.但由于Java中无法直接对bit进行操作，没有这个数据结构。取而代之的是一个辅助数据结构： `BitSet` ，它只是一个包装版的Bit集合，它的内存占用最少，但性能不高（内部基于long数组存储，通过移位与取模运算来实现）。因此为了性能表现更好，只能选择原生的数组结构（long,int,char,byte，它们的占用内存空间按序在递减，性能却在按序递增），它们的内存空间都是连续的，而且一个数据块儿就存放一个数据记录，无需像 `BitSet` 一样要再次判断bit位位置，但存储1,0开关占用的内存比bit大得多。因此我们选择byte[]作为二级索引实现，跟 `BitSet` 对比，也算是空间换时间了。
因此一个完整的数据结构为： `Map<String, byte[]>` 第一层找到自身对应的队列，第二层找到当前队列的授权开关数组（byte[]）。通过2次 O(1)即可判断某个队列是否有跟另一个队列交互的权限。


##Best Practice
* 资源的对应关系：一个 `Messagebus`对象对应一个 rabbitmq `Connection`，对应N个`Channel`（Channel由Connection创建，此处的N取决于Channel池的配置）
* 客户端的主对象 `Messagebus` 最好在同一个地址空间（JVM进程）内进行共享，客户端应该尽可能缩短其生命周期，做到 **最晚打开，最早关闭**
* 消息carry的主对象，在 `Messagebus` 被实例化时创建，因此其也是默认被实现为单例的。可将其视为 ***helper*** 对象
* 每个消息在carry的过程中，都拥有自己的上下文对象(`MessageContext`)
* 消息以异步消费的模式下，因为是派生出一个独立的线程来做event loop，因此，在客户端不想继续消费时，有两种方式对其进行关闭：（1）consume方法返回一个 `IConsumerCloser` 的实例用于关闭；（2）消息产生事件，携带的一个参数即为 `IConsumerCloser` 的实例可以随时关闭event loop.



[1]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/client/messagebus-client-diagram-1.png
[2]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/client/carry-inherits.png
[3]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/client/messagebus-client-diagram-2.png
[4]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/client/handle-chain.png
[5]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/client/node.png
[6]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/client/handler-chain-config.png
[7]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/client/message-context.png
[8]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/client/channel-pool.png
[9]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/client/zookeeper-node.png
[10]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/client/param-validator.png
[14]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/client/node-db-info.png
[15]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/client/req-resp.png
[16]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/client/partofroutertopology.png