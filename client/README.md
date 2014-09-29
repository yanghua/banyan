#提纲
[名词解释](#名词解释)

[消息传输](#消息传输)

[消息格式](#消息格式)

[消息的链式处理](#消息的链式处理)

[消息处理的上下文对象](#消息处理的上下文对象)

[Channel的对象池](#Channel的对象池)

[远程配置与管控](#远程配置与管控)

[动态添加处理器](#动态添加处理器)

[调用示例](#调用示例)

##名词解释
- message carry: 消息的传输（`produce` `consume` `request` `response` ）被抽象为carry(表示消息的 **搬运** )

##消息传输
消息传输被抽象为四个接口：

* IProducer: 定义了生产消息的接口
* IConsumer: 定义了消费消息的接口
* IRequest: 定义了发送请求消息的接口
* IResponse: 定义了发送响应/应答消息的接口
* AbstractMessageCarryer: 抽象了消息传输的共性部分（主要包含handler-chain的实例化）

继承关系图如下：

![img 2][2]

> 其中，IRequest/IResponse是用于模拟req/resp的类http模式的消息处理方式。


##消息的链式处理
消息的链式处理，有利于切割处理模块，方便拆分功能等。我认为这种方式应该是以数据为处理核心的业务模型的首选。
这部分的继承关系图如下：
![img 4][4]

AbstractHandler处于继承类的顶端，为一个抽象的处理器，它定义了三个方法：

* init: 实例化方法，在handler从配置文件中读取并初始化的时候被调用
* handle: 每个继承它的handler所必须实现的 **抽象** 方法，是实现handler-chain的关键
* destroy: 释放资源的触发方法，将在“关闭”messagebus client的时候被逐一调用

从上面的图示可以看到，所有的handler被分为三大类（分别位于五个package中）:

* common: 公共handler包，用于封装p & c都需要处理的逻辑，比如参数校验等
* produce: 在生产消息过程中，需要的handler
* consumer: 在发送消息过程中，需要的handler
* request: 在发送请求消息的过程中，需要的handler
* response: 在发送响应消息的过程中，使用到的handler

###produce 的处理链
如下图:

![img 10][10]

###consume 的处理链
如下图:

![img 11][11]

###request 的处理链
如下图:

![img 12][12]

###response 的处理链
如下图:

![img 13][13]

将处理逻辑以handler进行切分，不仅有利于业务隔离，同时也有利于通过组合进行很方便的重用。这里重用得比较多得几个handler有：

* MessageId-Generator
* Pooled-Channel-Handler
* Validate-Handler

当然，他们的先后顺序并不是定死的，而是依赖于配置，配置文件即为该[handler.xml](https://github.com/yanghua/messagebus/blob/master/client/src/main/resources/handler.xml) 文件
![img 6][6]

前面提到的AbstractHandler以及所有的子handler都是用于承载业务实现的，但要串联起它们，就需要另一个接口：

* IHandlerChain: 定义了一个handle方法，用于实现handler-chain
* MessageCarryHandlerChain: 实现了 ***IHandlerChain*** ,并构建了一个用于消息传输的处理器链

##req/resp
req/resp是用于模拟请求/应答的消息交互模型，这也是大部分基于tcp协议的交互方式。但这种模型也是比较常见的，比如有一个队列专门用于授权服务。那么它就是一个比较常见的阻塞式的请求/应答模型。这种模型如果不封装，用通常的produce/consume模型比较难于实现。因为消息总线的消息传递方式默认是 ***异步*** ***不耦合*** 的。下面给出了这种模式的实现方式：

![img 15][15]

通常会认为这种模式，通过p/c listen的队列，中转一下即可，也就是总路线1 与路线 2-*。但最终发现，无论找路由拓扑中的某个节点，都会破坏队列的 **职责单一性** ，如果一个队列接收多种不同的消息，而且是混杂式的并且是不同的消费场景，将会使消费端变得混乱、不可控。所以最终的做法是，在produce发往queue-1的时候，以其发送的消息ID，创建一个挂载在rabbitmq上的一个默认的exchange上得临时队列。客户端收到以后，往该临时队列中发送响应信息，producer接收到响应信息后，将临时队列删除。


##分布式消息Id
消息总线中，消息的生产者是分布式的。在这里的消息Id的生成，借助于Twitter开源的分布式消息ID生成方案。
> 它基于 `Snowflake` 算法，据测试提供的数据：snowflake每秒可以产生26万左右的ID。它的ID基于一定的规则生成：41位毫秒级时间戳+5位datacenter标识+5为节点标识+12位毫秒内序列，另外在加首位的标识符，共64位。
> 用这种算法的好处是，它既能生成唯一的串，同时也是整体在时间上自增排序的，并且在整个分布式系统内不会产生ID碰撞


##消息处理的上下文对象
之前提到了消息的处理是基于链式（或者称之为流式）的，但要让这些handler在技术层面上能够得以实现，一个承载了它们都需要用到的数据的上下文对象必不可少。
这里的上下文对象，就是所谓的“Big Object”，结构图如下：

![img 7][7]

因为这个对象主要流通于各个handler之间，为共享数据为目的。如果你需要设置额外的对象，而context原先并不包含，那么你可以通过其中的otherParams属性来扩展你需要传输的数据它接收键值对集合<String, Object>。

##Channel的对象池
如果有大批量或生产消息密集型的业务需求，那么每次生产消息都创建然后再销毁用于发送消息的channel，有些过于浪费。channel的初始化涉及到socket的通信、连接的建立，毫无疑问它也是那种真正意义上的“big object”。所以在高频率使用的场景下，池化channel无疑能够提供更好的性能。
此处，对象池的底层支持基于apache的common-pool，它的实现位于 ***com.freedom.messagebus.client.core.pool*** package下，关键对象：
![img 8][8]

> PooledChannelAccessor 并不是Pool实现的一部分，它是一个handler，应该说是使用者。

* AbstractPool: 一个被实现为泛型的通用抽象pool，它以 ***face pattern*** 实现，内部以一个GenericObjectPool的实例来提供服务
* ChannelPool: 继承自AbstractPool，并对其进行了具体化以支持对channel的cache
* ChannelPoolConfig: 继承自GenericObjectPoolConfig，用于对pool进行一些设置，这里预先进行了部分设置，还有部分位于配置文件
* ChannelFactory: 继承自PooledObjectFactory，用于创建被 **包装** 过的真实对象

##远程配置与管控
zookeeper 作为hadoop的子项目，初衷是用来作为集群配置管理。在消息总线中，zookeeper被用来做 **集群配置变更主动推送** 和 **系统事件推送** 以及 **路由信息的变更推送** 
目前zookeeper server的节点结构图：
![img 9][9]

这三个节点分别对应着上面所说的三种功能：

- router:路由信息的配置只能通过消息总线管理系统的图形化界面才能进行变更，变更后的信息会被设置到该节点，然后推送到各个client
- config:client的管控配置信息，可以实现阈值等相关控制功能
- event:管理控制台往各client推送的事件

为什么要采用zookeeper实现这些功能？
zookeeper本身就是用来做配置变更管理，因此此处部分应用了它擅长的一面。不过它的另一个好处是主动推送（广播），所谓的推送从原理上来讲即是侦听某个端口的长连接socket，而所谓的事件常用的实现方式也是thread + loop + message的形式提供出来。因此这里用它来实现客户端的系统事件，是非常好的减轻消息总线socket连接数的实现方式。

客户端准备carry message的时候，需要传入zookeeper的server host 以及 port。紧接着，会从remote端同步最新的config，然后实例化本地的config manager。与此同时，会建立起长连接的zookeeper client（见 **LongLiveZookeeper** ），它会根据同步过来的path列表，侦听所有server端的这些配置，一旦有任何对队列配置的修改，就会被同步到client来，这样就可以在client即时应用这些策略，来对其加以管控。这可以实现很多需求，比如：

```
* 禁止往erp队列发送任何消息
* 只可以往crm队列发送text消息
* 往oa.sms里发送的消息不得大于500K
* 往oa.email里发送消息的速率不得大于10条/s
* ...
```

###router 信息
消息总线的路由方式依赖于rabbitmq的topic模式 + 一个树形的拓扑结构。而树的节点只有两种类型：exchange 、 queue。
这里它们都被抽象为数据结构： `Node`.

其结构如下：
![img 5][5]

它是一个简单的Java POJO，从图中的property信息也可以看出：它的定义支持了树形结构（`parentId`/`level`）。另外：

- type：标示了它的类型
- routerType：标示了当前exchange属于哪种类型
- routingKey：标示了它的路由路径
- name：节点的对外公开名称
- value：标识了它的完全限定名

它对应的数据库表结构形如：
![img 14][14]

这些配置一旦有变更，就会被理解推送到连接了zookeeper的client，client会将其先保存在 `/tmp/`下，然后理解对其进行解析、装载。消息在进行下一次carry时即会基于新的策略。而这些信息对client以外（app）则是完全透明的。客户端只需要了解需要发送的queue的`name`即可。


##动态添加处理器
上面提到的 **远程配置与管控** 关键点在于：已在client的jar包中实现了代码逻辑（通过client的代码逻辑结合动态配置的参数来实现）。但zooKeeper+jvm 支持的 Class loader机制能够带来更加强大的功能：远程加载java类的字节码文件，通过这种机制，我们可以达到 **临时** 添加handler的功能。这种“打补丁”不重启的策略能够很好的对client进行临时性的透明升级（不需要重新编译然后生成新的jar文件再分发到客户端）！

具体的实现方式如下：
在管控端维护着client实现的一份源码（这是前提条件），添加新的handler，编译并生成该handler的 ***.class*** 文件，将其放置在预先配置的zookeeper节点下，然后再辅以对该handler的说明性配置。客户端感知到之后配置更新后，会通过一个 `RemoteClassLoader` 用下载下来的字节码的二进制文件构建出该类的 `Class` 对象的实例，然后通过反射产生该类对象的实例并向上转型为 `AbstractHandler` 的实例，再将其加入相应的handler chain集合中，即可产生一个新的处理环节。

这种方式带来的限制：

* 这个动态的处理器只在一个 `Messagebus` 的存活周期内有效（也就是说当调用client.close()后，它就会消失），下一次还是需要重新下载并构建
* 必须依赖一个客户端已存在的接口实现，不然无法利用多态机制进行强制向上转型

但毫无疑问，这是动态打补丁或临时救急的一种有效方式！



##调用示例

```java
public void testSimpleProduceAndConsume() throws Exception {
        Messagebus client = Messagebus.getInstance();
        client.setZkHost("localhost");
        client.setZkPort(2181);
        client.open();

        //start consume
        appkey = java.util.UUID.randomUUID().toString();
        msgType = "business";
        String queueName = "oa.sms";
        IConsumerCloser closer = client.getConsumer().consume(appkey, msgType, queueName,
                                                              new IMessageReceiveListener() {
            @Override
            public void onMessage(Message msg, MessageFormat format) {
                switch (format) {
                    case Text: {
                        TextMessage txtMsg = (TextMessage) msg;
                        logger.debug("received message : " + txtMsg.getMessageBody());
                    }
                    break;

                    case Object: {
                        ObjectMessage objMsg = (ObjectMessage) msg;
                        SimpleObjectMessagePOJO realObj = (SimpleObjectMessagePOJO) objMsg.getObject();
                        logger.debug("received message : " + realObj.getTxt());
                    }
                    break;

                    //case other format
                    //...
                }
            }
        });

        //produce text msg
        TextMessagePOJO msg = new TextMessagePOJO();
        msg.setMessageBody("just a test");
        client.getProducer().produce(msg, MessageFormat.Text, appkey, queueName, msgType);

        //produce object msg
        ObjectMessagePOJO objMsg = new ObjectMessagePOJO();
        SimpleObjectMessagePOJO soPojo = new SimpleObjectMessagePOJO();
        soPojo.setTxt("test object-message");
        objMsg.setObject(soPojo);
        client.getProducer().produce(objMsg, MessageFormat.Object, appkey, queueName, msgType);

        //sleep for checking the result
        Thread.sleep(10000);
        closer.closeConsumer();

        client.close();
    }  
```


###关于调用示例的说明
（1）Messagebus

代表客户端的关键对象，其被实现为单例模式。因此获得其实例，需要调用其静态方法：


```
Messagebus client = Messagebus.getInstance();
```

在前面提及过，客户端通过zookeeper来同步远程配置。所以，在获得 `Messagebus` 的实例之后，关键的一步就是设置zookeeper的host以及port:

```
client.setZkHost("localhost");
client.setZkPort(2181);
```
注意，如果不显式设置，则Messagebus则分别对这两个字段采用 **localhost** 及 **2181** 进行初始化。

设置完必要的属性之后，需要调用 `Messagebus` 的实例来初始化关键对象：

```
client.open();
```

这些关键对象对于消息的carry至关重要，但从另一方面来说他们也都是“昂贵资源”。所以，在确认不再carry消息后，需要尽快释放这些资源：

```
client.close();
```

通过 `Messagebus` 的实例，可以获得producer以及consumer，他们也是carry消息的真实对象：

```
client.getProducer()
client.getConsumer()
```

在生产消息的时候需要构建各种格式的消息对象，在消费消息的时候，有一个值得注意的地方。由于消息的生产和消费对于程序的实现模型有着本质的不同（通常，生产是瞬时性的，而消费是long event loop的），在消费的时候因为内部在一个独立的线程上构建有一个event loop，如果不想继续消费，需要关掉它。这里是通过一个 ***IConsumerCloser*** 实现的。在消费的时候，会将构建有event loop的线程的控制权（说白了就是它的引用）下放给该接口的实现者（该接口定义了一个closeConsumer方法），通过该接口的实例上调用closeConsumer来关闭consumer:

```
closer.closeConsumer();
```

(2)IProducer

关于生产消息的接口，目前开放了四个接口方法：

* 单个消息的简单发送
* 单个消息的事务型发送
* 批量消息的简单发送
* 批量消息的事务型发送

这里包含两个关键参考点：（1）是否是批量发送；（2）是否为事务型发送

是否批量发送：这里开放单个消息的发送接口，主要是为了客户端调用方便，这样如果只发送一条消息就没有必要构建一个消息数组（虽然在处理的过程中还是会被封装为数组，这也是为了处理逻辑上的一致简洁性），但如果发送前已经明确有超过一条消息即将发送，还是推荐使用批量发送接口，这会省去获取Channel的开销，因为即便采用pool的机制，也还是有个获取的过程

是否为事务型发送：这里为了某些必须确认消息是否送达的安全性较高的场景而提供，事实上如果消息的安全性没有那么重要（事实上消息中间件server端的持久化机制已经给安全性提供了基本的保障），**不推荐** 使用事务型发送接口，因为它将对每条发送的消息给予应答确认。

(3)IConsumer

消费消息的接口比较简单，只有一个。值得一提的是，在调用该接口的时候，需要传递入一个消息处理的回调。该接口即为 `IMessageReceiverListener` ，它包含有一个onMessage方法，在获取到消息之后，将触发该方法的调用，它提供给业务处理方两个参数：

* Message: 消息对象的顶层语义接口
* MessageFormat: 消息的格式

通过这两个对象，就足以还原并获取真实的消息。（根据消息格式，对 `Message` 的实例进行强制向下转型到特定的消息格式对象），具体请参看示例调用代码。





[1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/message-inherits.png
[2]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/carry-inherits.png
[3]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/message-formatter-inherits.png
[4]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/handle-chain.png
[5]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/node.png
[6]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/handler-chain-config.png
[7]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/message-context.png
[8]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/channel-pool.png
[9]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/zookeeper-node.png
[10]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/produce-chain.png
[11]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/consume-chain.png
[12]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/request-chain.png
[13]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/response-chain.png
[14]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/node-db-info.png
[15]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/req-resp.png