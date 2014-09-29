#overview

scenario 并不是消息总线的必备项目。它的主要目的是用于展示：

- 消息总线client的使用场景
- 测试消息总线的功能
- 测试消息总线client的API的合理性
- 测序消息总线的性能

目前提供的场景有单纯得：`produce`、`consume`、`request`、`response`

##produce
produce的使用方式见：`ProduceTemplate`

produce 的使用方式属于那种 `开箱即用`的模式，消息正常都是按需发送，因此Messagebus的client生命周期很短，通常的建议是 ***快速打开，快速关闭*** 。
大致的代码段如下：

```java
public static void produce() {
        Message msg = MessageFactory.createMessage(MessageType.AppMessage);
        String queueName = "crm";

        AppMessageBody appMessageBody = (AppMessageBody) msg.getMessageBody();
        appMessageBody.setMessageBody("test".getBytes());

        Messagebus client = Messagebus.getInstance(appkey);
        client.setZkHost(host);
        client.setZkPort(port);

        try {
            client.open();
            client.getProducer().produce(msg, queueName);
        } catch (MessagebusConnectedFailedException | MessagebusUnOpenException e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
```

##consume
consume的使用方式见： `ConsumeTemplate`

consume 的使用方式属于那种service模型，因为你不知道消息什么时候会来到，通常都采用：`阻塞侦听`的模式，消息的处理时机未知，因此消费端的Messagebus对象生命周期较长。常用的场景：

* 在`Application_Start`之时以一个独立的线程打开；在`Application_End`之时关闭
* 独立的宿主服务

但这种模式有一个约束就是：你如何hold这个消息的消费线程，让它交出控制权的同时，同时要避免在主线程方法执行完成之后因为退出而导致消费线程也一同退出的问题。这里采用的方式是，以对象锁的方式来实现线程同步，代码段如下：

```java
public static class ConsumerService extends Thread {

        Messagebus client = Messagebus.getInstance(appkey);

        String          appName        = "crm";
        IConsumerCloser consumerCloser = null;
        private final Object lockObj = new Object();

        @Override
        public void run() {
            try {
                synchronized (lockObj) {
                    //set zookeeper info
                    client.setZkHost(host);
                    client.setZkPort(port);

                    client.open();
                    IConsumer consumer = client.getConsumer();
                    consumerCloser = consumer.consume(appName, new IMessageReceiveListener() {
                        @Override
                        public void onMessage(Message message) {
                            logger.info("[" + message.getMessageHeader().getMessageId() +
                                            "]-[" + message.getMessageHeader().getType() + "]");
                        }
                    });

                    logger.info("blocked for receiving message!");
                    lockObj.wait(0);
                    logger.info("released object lock!");
                }
            } catch (IOException | MessagebusUnOpenException |
                MessagebusConnectedFailedException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                logger.info("close client");
                consumerCloser.closeConsumer();
                client.close();
            }
        }

        public void stopService() {
            //style 1 : use lock released
            synchronized (lockObj) {
                lockObj.notifyAll();
            }

            //style 2 : use interrupt
//            this.interrupt();
        }
    }
```

##request
request的使用方式见： `RequestTemplate`

request的使用场景跟produce类似，但不同的是，它发送消息后以阻塞的模式等待响应，同时可以设置一个等待的timeout，如果在设置的时间之内未能获得结果，将得到一个超时异常。代码段如下：

```java
public static void main(String[] args) {
        Messagebus messagebus = Messagebus.getInstance(appkey);
        messagebus.setZkHost(host);
        messagebus.setZkPort(port);

        Message msg = MessageFactory.createMessage(MessageType.AppMessage);
        String queueName = "crm";

        AppMessageBody appMessageBody = (AppMessageBody) msg.getMessageBody();
        appMessageBody.setMessageBody("test".getBytes());

        Message respMsg = null;

        try {
            messagebus.open();
            IRequester requester = messagebus.getRequester();

            respMsg = requester.request(msg, queueName, 10);
            //use response message...
            logger.info("response message : [" + respMsg.getMessageHeader().getMessageId() + "]");
        } catch (MessagebusConnectedFailedException | MessagebusUnOpenException |
            MessageResponseTimeoutException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            messagebus.close();
        }
    }
```

##response
response的使用方式见 `ResponseTemplate`

response的使用场景跟consume类似，只是consume的一个变种，也就是说收到消息后处理，然后给出响应。代码段如下：

```java
public static class ResponseService extends Thread {

        Messagebus client = Messagebus.getInstance(appkey);

        String          appName        = "crm";
        IConsumerCloser consumerCloser = null;
        private final Object lockObj = new Object();

        @Override
        public void run() {
            try {
                synchronized (lockObj) {
                    //set zookeeper info
                    client.setZkHost(host);
                    client.setZkPort(port);

                    client.open();
                    IConsumer consumer = client.getConsumer();
                    final IResponser responser = client.getResponser();
                    consumerCloser = consumer.consume(appName, new IMessageReceiveListener() {
                        @Override
                        public void onMessage(Message message) {
                            //handle message
                            String msgId = String.valueOf(message.getMessageHeader().getMessageId());
                            logger.info("[" + msgId +
                                            "]-[" + message.getMessageHeader().getType() + "]");

                            //send response
                            responser.responseTmpMessage(message, msgId);
                        }
                    });

                    logger.info("blocked for receiving message!");
                    lockObj.wait(0);
                    logger.info("released object lock!");
                }
            } catch (IOException | MessagebusUnOpenException |
                MessagebusConnectedFailedException | InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                logger.info("close client");
                consumerCloser.closeConsumer();
                client.close();
            }
        }

        public void stopService() {
            //style 1 : use lock released
            synchronized (lockObj) {
                lockObj.notifyAll();
            }

            //style 2 : use interrupt
//            this.interrupt();
        }
    }
```

##关于调用示例的说明
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

通过 `Messagebus` 的实例，可以获得producer、consumer、request、response，他们也是carry消息的真实对象：

```
client.getProducer()
client.getConsumer()
client.getRequester()
client.getResponser()
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

消费消息的接口比较简单，只有一个。值得一提的是，在调用该接口的时候，需要传递入一个消息处理的回调。该接口即为 `IMessageReceiverListener` ，它包含有一个onMessage方法，在获取到消息之后，将触发该方法的调用，它提供给业务处理方消息对象：

* Message: 消息对象