#overview
message bus can be used to communicate and integrate over multi-app. It used a [RabbitMQ](http://www.rabbitmq.com/) as backend broker(message exchanger). Most scenario:

* enterprise information Integration
* oriented-component & oriented-module distributed developer
* provide support for simple esb or soa

the necessity of encapsulating with RabbitMQ:

* provide router pattern
* embed permission into client-jar
* removed create & delete & update operation from client, replaced with central-register mode


##router with tree topology structure
the message bus's implementation is based on Rabbitmq. It can takes advantage of multiple message exchange-types rabbitmq provided and builds many kinds of router pattern. The message bus's router topology lists below:


![img 3][3]

the advantages of the tree topology:

* hide the router topology from client-caller (just need to know the `proxy` node)
* multiple message communication pattern (p2p, pub/sub, broadcast)
* implement the message log without interrupting the message channel
* communication-policy configure once , push everywhere

##client
###naming
message carry: message transport（`produce` `consume` `request` `response` `publish` `subscribe` `broadcast` ）were be abstracted to  carry

###message transport
all supported carry type：

* IProducer
* IConsumer
* IRequestor
* IResponser
* IPublisher
* ISubscriber
* IBroadcaster
* AbstractMessageCarryer

the relation of inheritance：

![img 4][4]

> what's more，IRequest/IResponse use to simulate req/resp message process like http。


###the process chain of message
the process of message used the pattern `Pipeline-Filter Pattern` [POSA 4]，it's good at spliting module or data. 

the relation of inheritance：
![img 5][5]

AbstractHandler can be considered as a hander，it defined three methods：

* init: initialization method
* handle: every handler should be implemented
* destroy: release resource method

the sequence of order is defined in config file[handler.xml](https://github.com/yanghua/messagebus/blob/master/client/src/main/resources/handler.xml) 
![img 6][6]


* IHandlerChain: defined handle method，used to implement `handler-chain`
* MessageCarryHandlerChain: implemented ***IHandlerChain*** ,and built a handler-chain for processing message

###router info
the message's router model depends the rabbitmq's topic mode and a tree topology structure. And the tree node just has two types: `exchange`, `queue`.

here, both of them were be abstracted as a structure `Node`.

it's structure listed below：
![img 7][7]

It is a simple Java POJO.

this is data base's table schema:
![img 8][8]

##server
the message bus also depends some core service build around the RabbitMQ. These service run as a long-time deamon service host in a server. the service itself is the user of `client` and it use queue `proxy.message.system.server`.

`server` contains three parts：

- app : the enter of server and supported the start-logic 
- bootstrap : server's core service，Generally it contains：rabbitmq-server's startup/zookeeper's startup；they start sychorizely and can noe be failure.
- daemon : defined some daemon service

##interactor-component
the module encapsulated the interaction with RabbitMW (used rabbitmq-java-client). and stop the direct dependency of rabbitmq-java-client

now it contains three parts:

* rabbitmq : encapsoluted rabbitmq中exchange/queue/channel/topology operate interface
* proxy : provided produce and consume message's api
* message : provided message header / body 's adapter and box and unbox message


##common-component
common component provides some common structure's definition(such as message header and message body) and some common util methods.

the message's definition:

![img 9][9]

##business-component
it is the message bus's business component

##httpbridge
it's the http api of message bus that used to connect those apps built without java technology.

###restful
####produce：

```
/messagebus/queues/{qname}/messages?appkey={appkey}&type={produce}
```

http method : `POST`

request params :

* path : qname - queue name
* querystring : 
	* appkey - auth key （must）
	* type - identify API，value `produce` （must）
* request body : 
	* messages - message object list （must）
	
response :

```js
{
	statusCode: 10200,
	error: "",
	msg: "",
	data: ''
}
```

####consume:

```
/messagebus/queues/{qname}/messages?appkey={appkey}&type={consume}&mode={sync}&num={num}
```

http method : `GET`

request params : 

* path : qname - queue name
* querystring : 
	* appkey - auth key （must）
	* type - identify API，value `consume` （must）
	* mode - value `sync` or `async` （must）
	* num - except num，from  0 < num to 100(equals) （mode must be sync）
	
response : 

```js
{
	statusCode: 10200,
	error: "",
	msg: "",
	data: [
		{
			messageHeader: {
				messageId: 520133271997313000,
				type: "appMessage",
				timestamp: null,
				priority: 0,
				expiration: null,
				deliveryMode: 2,
				headers: null,
				contentEncoding: null,
				contentType: null,
				replyTo: null,
				appId: null,
				userId: null,
				clusterId: null,
				correlationId: null
			},
			messageBody: {
				messageBody: [
					116,
					101,
					115,
					116
				]
			},
			messageType: "AppMessage"
		}
	]
}

```

####request:

```
/messagebus/queues/{qname}/messages?appkey={appkey}&type={type}&timeout={timeout}
```

http method : `POST`

request params :

* path : qname - queue name
* querystring : 
	* appkey - auth key（must）
	* type - identify API，value `request`（must）
	* timeout - timeout，unit microsecond（must）
* request body : 
	* message - message object （client blocked and just once）
	
response :

```js
{
	statusCode: 10200,
	error: "",
	msg: "",
	data: {
			messageHeader: {
				messageId: 520133271997313000,
				type: "appMessage",
				timestamp: null,
				priority: 0,
				expiration: null,
				deliveryMode: 2,
				headers: null,
				contentEncoding: null,
				contentType: null,
				replyTo: null,
				appId: null,
				userId: null,
				clusterId: null,
				correlationId: null
			},
			messageBody: {
				messageBody: [
					116,
					101,
					115,
					116
				]
			},
			messageType: "AppMessage"
	}
}
```


####response:

```
/messagebus/queues/{qname}/messages?appkey={appkey}
```
http method : `POST`

request params : 

* path : qname - queue name
* querystring : 
	* appkey - auth key（must）
	* type - identify API，value `response`（must）
* request body :
	* message - message object
	
response :

```js
{
	statusCode: 10200,
	error: "",
	msg: "",
	data: ''
}
```

##scenario
scenario is used to show:

* message bus's client use-scenario
* test message bus's function
* test message bus's client api

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

more scenario please see the module `scenario`

##benchmark
it shows the  `client` performance:

###hardware
client : 

```
OS : Mac os x Yosemite (version 10.10)
Processor : 2.5GHz Intel Core i5
Memory : 8GB 1600 MHz DDR3
JDK Version : 1.7.0_45
```

server :

```
OS : Ubuntu Server 14.04.1 (GNU/Linux 3.13.0-37-generic x86_64)
Processor : Intel(R) Xeon(R) CPU E3-1230 V2 @ 3.30GHz (8核)
Memory : 8GB
JDK Version : 1.7.0_72
```

###produce
* single thread，multiple message size ，cycle send，compare：

![img 10][10]

* single thread，same message size，use client channel pool or not，compare：

![img 11][11]

###consume
* single thread，multiple message size，async receive，compare：

![img 12][12]

* single thread，same message size，use client channel pool or not，compare：

![img 13][13]



[1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/overview/architecture.png
[2]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/overview/module-dependency.png
[3]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/overview/router-topology.png
[4]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/carry-inherits.png
[5]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/handle-chain.png
[6]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/handler-chain-config.png
[7]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/node.png
[8]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/client/node-db-info.png
[9]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/common/message-design.png
[10]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/benchmark/produce/singleThreadClientVSOriginal.png
[11]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/benchmark/produce/singleThreadOptionPool.png
[12]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/benchmark/consume/singleThreadClientVSOriginal.png
[13]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/benchmark/consume/singleThreadOptionPool.png