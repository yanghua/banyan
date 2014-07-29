#提纲
[名词解释](#名词解释)

[消息传输](#消息传输)

[消息格式](#消息格式)

[消息的链式处理](#消息的链式处理)

[消息处理的上下文对象](#消息处理的上下文对象)

[Channel的对象池](#Channel的对象池)

[远程配置与管控](#远程配置与管控)

[调用示例](#调用示例)

##名词解释
- message carry: 消息的produce / consume 被抽象为carry(表示消息的 **搬运** )
- message format: object/text等，注意此处没有称之为message type，请注意区分
- message type: business/system 按照业务逻辑来划分

##消息传输
消息传输分为produce / consume 被抽象为两个接口：

* IProducer: 定义了生产消息的接口
* IConsumer: 定义了消费消息的接口
* AbstractMessageCarryer: 抽象了消息传输的共性部分（主要包含handler-chain的实例化）

继承关系图如下：
![img 2][2]



##消息格式
参照jms规范，目前messagebus支持如下五种消息：

* Stream - 流
* Bytes - 字节数组
* Map - map(键值对)
* Object - 对象类型
* Text - 文本类型

继承关系如下图：

![img 1][1]

消息中间件默认只接受byte[]，因此需要对以上支持的消息进行格式化，这部分对应的继承关系图：
![img 3][3]

其中：

* IFormatter: 为消息格式化器接口，提供了消息格式化的两个契约方法：
    - format(Message): 为消息格式化方法，用于produce
    - deFormat(byte[]): 为消息反格式化方法，用于consume
* FormatterFactory: 提供了formatter的创建工厂

##消息的链式处理
消息的链式处理，有利于切割处理模块，方便拆分功能等。我认为这种方式应该是以数据为处理核心的业务模型的首选。
这部分的继承关系图如下：
![img 4][4]

AbstractHandler处于继承类的顶端，为一个抽象的处理器，它定义了三个方法：

* init: 实例化方法，在handler从配置文件中读取并初始化的时候被调用
* handle: 每个继承它的handler所必须实现的 **抽象** 方法，是实现handler-chain的关键
* destroy: 释放资源的触发方法，将在“关闭”messagebus client的时候被逐一调用

从上面的图示可以看到，所有的handler被分为三大类（分别位于三个package中）:

* common: 公共handler包，用于封装p & c都需要处理的逻辑，比如参数校验等
* produce: 在生产消息过程中，需要的handler
* consumer: 在发送消息过程中，需要的handler

目前已经支持的handler的文件目录结构图
![img 5][5]

当然，他们的先后顺序并不是定死的，而是依赖于配置：
![img 6][6]

前面提到的AbstractHandler以及所有的子handler都是用于承载业务实现的，但要串联起它们，就需要另一个接口：

* IHandlerChain: 定义了一个handle方法，用于实现handler-chain
* MessageCarryHandlerChain: 实现了 ***IHandlerChain*** ,并构建了一个用于消息传输的处理器链

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
可以看到在项目的resources文件夹下包含有多个配置文件，这里，他们存在的目的主要是初期构建的需要。等到真正使用的时候，他们会被“无视”或者是成为失效备援而采用。所有的配置都将从远程获取，这依赖于用zooKeeper实现的远程配置集群。
目前初步构建的远程zookeep节点示例：
![img 9][9]

拆分为两个部分来看：

* common-config: client通用配置，比如对象池配置、path配置、queue的命名名称配置等
* proxy: 队列的“个性化配置”，此节点的子树跟route topology基本一致，需要注意的是只有队列（图中该分支里的圆形）才拥有这些个性化的配置。用于客户端对发往这些队列的消息进行管控

> 挂有"data"的数据节点才表示某节点带有配置，exchange通常不需要有配置（但如果需要也可以有）

客户端准备carry message的时候，需要传入zookeeper的server host 以及 port。紧接着，会从remote端同步最新的config，然后实例化本地的config manager。与此同时，会建立起长连接的zookeeper client（见 **LongLiveZookeeper** ），它会根据同步过来的path列表，侦听所有server端的这些配置，一旦有任何对队列配置的修改，就会被同步到client来，这样就可以在client即时应用这些策略，来对其加以管控。这可以实现很多需求，比如：

```
* 禁止往erp队列发送任何消息
* 只可以往crm队列发送text消息
* 往oa.sms里发送的消息不得大于500K
* 往oa.email里发送消息的速率不得大于10条/s
* ...
```

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





[1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/message-inherits.png
[2]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/carry-inherits.png
[3]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/message-formatter-inherits.png
[4]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/handle-chain.png
[5]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/handler-chain-structure.png
[6]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/handler-chain-config.png
[7]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/message-context.png
[8]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/channel-pool.png
[9]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/zookeeper-node.png