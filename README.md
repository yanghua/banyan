#overview

![img 14][14]


```
banyan is a tree has thick branches which grows in the East Asia. 
```
Thanks for [@ok95](https://github.com/ok95) & [@Joy-Zhang](https://github.com/Joy-Zhang) given the good idea & guidance.

it can be used to communicate and integrate over multi-app. It used a [RabbitMQ](http://www.rabbitmq.com/) as backend broker(message exchanger). Most scenario:

* enterprise information Integration
* oriented-component & oriented-module distributed developer
* provide support for simple esb or soa

the necessity of encapsulating with RabbitMQ:

* provide router pattern
* embed permission into client-jar
* removed create & delete & update operation from client, replaced with central-register mode


##tree topology structure
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

##scenario & usage
scenario is used to show:

* message bus's client use-scenario
* test message bus's function
* test message bus's client api

###produce & consume

```java
private static void produce() {
        //crm
        String appid = "djB5l1n7PbFsszF5817JOon2895El1KP";
        Messagebus client = new Messagebus(appid);
        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

        try {
            client.open();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        }

        Message msg = MessageFactory.createMessage(MessageType.QueueMessage);
        msg.getMessageHeader().setContentType("text/plain");
        msg.getMessageHeader().setContentEncoding("utf-8");

        QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
        body.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));

        msg.setMessageBody(body);

        client.produce(msg, "erp");

        client.close();
    }
```


```java
private static void consumeWithPullStyle() {
        //erp
        String appid = "D0fW8u2u1v7S1IvI8qoQg3dUlLL5b36q";
        Messagebus client = new Messagebus(appid);
        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

        try {
            client.open();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        }

        List<Message> msgs = client.consume(1);

        client.close();

        for (Message msg : msgs) {
            logger.info(msg.getMessageHeader().getMessageId());
        }
    }
```

```java
private static void ConsumeWithPushStyle() {
        //erp
        String appid = "D0fW8u2u1v7S1IvI8qoQg3dUlLL5b36q";
        Messagebus client = new Messagebus(appid);
        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

        try {
            client.open();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        }

        client.consume(new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {
                logger.info(message.getMessageHeader().getMessageId());
            }
        }, 5, TimeUnit.SECONDS);

        client.close();
    }
```

```java
private static void asyncConsume() {
        AsyncConsumeThread asyncConsumeThread = new AsyncConsumeThread();
        asyncConsumeThread.startup();

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        asyncConsumeThread.shutdown();
    }

    private static class AsyncConsumeThread implements Runnable {

        private Thread currentThread;

        public AsyncConsumeThread() {
            this.currentThread = new Thread(this);
            this.currentThread.setName("AsyncConsumeThread");
            this.currentThread.setDaemon(true);
        }

        @Override
        public void run() {
            //erp
            String appid = "D0fW8u2u1v7S1IvI8qoQg3dUlLL5b36q";
            Messagebus client = new Messagebus(appid);
            client.setPubsuberHost(host);
            client.setPubsuberPort(port);

            try {
                client.open();

                //long long time
                client.consume(new IMessageReceiveListener() {
                    @Override
                    public void onMessage(Message message) {
                        logger.info(message.getMessageHeader().getMessageId());
                    }
                }, Integer.MAX_VALUE, TimeUnit.SECONDS);
            } catch (MessagebusConnectedFailedException e) {
                e.printStackTrace();
            } finally {
                client.close();
            }
        }

        public void startup() {
            this.currentThread.start();
        }

        public void shutdown() {
            this.currentThread.interrupt();
        }
    }
```

####sync mode

```java
public static void main(String[] args) {
        Messagebus client = Messagebus.getInstance(appkey);
        client.setZkHost(host);
        client.setZkPort(port);

        IConsumer consumer = null;
        try {
            client.open();
            consumer = client.getConsumer();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        } catch (MessagebusUnOpenException e) {
            e.printStackTrace();
        }

        //consume
        List<Message> msgs = consumer.consume(appName, 2, 10_000);
        client.close();

        for (Message msg : msgs) {
            logger.info("message id : " + msg.getMessageHeader().getMessageId());
        }

    }
```

###request

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

###response

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

###publish

```java
public static void publish() {
        Message msg = MessageFactory.createMessage(MessageType.PubSubMessage);
        msg.getMessageHeader().setReplyTo("crm");
        msg.getMessageHeader().setContentType("text/plain");
        msg.getMessageHeader().setContentEncoding("utf-8");

        PubSubMessage.PubSubMessageBody body = new PubSubMessage.PubSubMessageBody();
        body.setContent("test".getBytes());
        msg.setMessageBody(body);

        Messagebus client = Messagebus.createClient(appId);
        client.setZkHost(host);
        client.setZkPort(port);

        try {
            client.open();
            client.getPublisher().publish(new Message[]{msg});
        } catch (MessagebusConnectedFailedException | MessagebusUnOpenException e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
```

###subscirbe

```java
public static class SubscribeService extends Thread {

        Messagebus client = Messagebus.createClient(appId);

        List<String>      subQueueNames    = new CopyOnWriteArrayList<>(new String[]{"crm"});
        ISubscribeManager subscribeManager = null;
        final Object lockObj = new Object();

        @Override
        public void run() {
            try {
                synchronized (lockObj) {
                    //set zookeeper info
                    client.setZkHost(host);
                    client.setZkPort(port);

                    client.open();
                    ISubscriber subscriber = client.getSubscriber();
                    subscribeManager = subscriber.subscribe(subQueueNames, new IMessageReceiveListener() {
                        @Override
                        public void onMessage(Message message, IReceiverCloser consumerCloser) {
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
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                logger.info("close client");
                subscribeManager.close();
                client.close();
            }
        }
```

###broadcast

```java
public static void broadcast() {
        String queueName = "crm";
        Message msg = MessageFactory.createMessage(MessageType.BroadcastMessage);
        msg.getMessageHeader().setReplyTo(queueName);
        msg.getMessageHeader().setContentType("text/plain");
        msg.getMessageHeader().setContentEncoding("utf-8");

        BroadcastMessage.BroadcastMessageBody body = new BroadcastMessage.BroadcastMessageBody();
        body.setContent("test".getBytes());
        msg.setMessageBody(body);

        Messagebus client = Messagebus.createClient(appId);
        client.setZkHost(host);
        client.setZkPort(port);

        try {
            client.open();
            client.getBroadcaster().broadcast(new Message[]{msg});
        } catch (MessagebusConnectedFailedException | MessagebusUnOpenException e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }
```

###http-resut

####produce

```java
public class ProduceTemplate {

    private static final Log logger = LogFactory.getLog(ProduceTemplate.class);

    private static final Gson gson = new Gson();

    private static String testUrlFormat = "http://%s:%s/messagebus/queues/%s/messages?appkey=%s&type=produce";
    private static String testHost      = "localhost";
    private static int    testPort      = 8081;
    private static String testQueue     = "crm";
    private static String appkey        = "jahksjdfhakjdflkasdjflk";

    public static void main(String[] args) {
        String url = String.format(testUrlFormat, testHost, testPort, testQueue, appkey);

        CloseableHttpClient httpClient = HttpClients.createDefault();

        CloseableHttpResponse response = null;

        Message msg = MessageFactory.createMessage(MessageType.QueueMessage);
        msg.getMessageHeader().setReplyTo(testQueue);

        QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
        body.setContent("test".getBytes());
        msg.setMessageBody(body);

        List<Message> msgs = new ArrayList<>(1);
        msgs.add(msg);
        String msgs2json = MessageJSONSerializer.serializeMessages(msgs);

        try {
            HttpPost postRequest = new HttpPost(url);
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("messages", msgs2json));
            postRequest.setEntity(new UrlEncodedFormEntity(nvps));

            response = httpClient.execute(postRequest);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                logger.info("response is : " + EntityUtils.toString(entity));
                long len = entity.getContentLength();
                if (len == -1)
                    logger.error("there is no response data.");
                else if (len < 2 * 1024 * 1024) {
                    logger.info("response is : " + EntityUtils.toString(entity));
                } else {
                    logger.error("[syncHTTPGet] response length is too large : (" + len + ") B " +
                                     "; and the url is : " + url);
                }
            }
        } catch (IOException e) {
            logger.error("[syncHTTPGet] occurs a IOException : " + e.getMessage());
        } finally {
            if (response != null)
                try {
                    response.close();
                } catch (IOException e) {
                    logger.error("[syncHTTPGet] finally block occurs a IOException : " + e.getMessage());
                }
        }
    }

}
```

####request

```java
public class RequestTemplate {

    private static final Log logger = LogFactory.getLog(RequestTemplate.class);

    private static String testUrlFormat = "http://%s:%s/messagebus/queues/%s/messages?appkey=%s&type=request&timeout=%s";
    private static String testHost      = "localhost";
    private static int    testPort      = 8081;
    private static String testQueue     = "crm";
    private static String appkey        = "jahksjdfhakjdflkasdjflk";
    private static long   timeout       = 5000;

    public static void main(String[] args) {
        String url = String.format(testUrlFormat, testHost, testPort, testQueue, appkey, timeout);

        CloseableHttpClient httpClient = HttpClients.createDefault();

        CloseableHttpResponse response = null;

        Message msg = MessageFactory.createMessage(MessageType.QueueMessage);
        msg.getMessageHeader().setReplyTo(testQueue);

        QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
        body.setContent("test".getBytes());
        msg.setMessageBody(body);

        String msg2json = MessageJSONSerializer.serialize(testMsg);

        try {
            HttpPost postRequest = new HttpPost(url);
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("message", msg2json));
            postRequest.setEntity(new UrlEncodedFormEntity(nvps));

            response = httpClient.execute(postRequest);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                long len = entity.getContentLength();
                if (len == -1)
                    logger.error("there is no response data.");
                else if (len < 2 * 1024 * 1024) {
                    logger.info("response is : " + EntityUtils.toString(entity));
                } else {
                    logger.error("[syncHTTPGet] response length is too large : (" + len + ") B " +
                                     "; and the url is : " + url);
                }
            }
        } catch (IOException e) {
            logger.error("[syncHTTPGet] occurs a IOException : " + e.getMessage());
        } finally {
            if (response != null)
                try {
                    response.close();
                } catch (IOException e) {
                    logger.error("[syncHTTPGet] finally block occurs a IOException : " + e.getMessage());
                }
        }
    }

}
```

####response

```java
public class ResponseTemplate {

    //there are two end points, follow there steps:
    /*
        send a request (end point 1)
        get a response (end point 2)
        send a response (end point 2)
        get a response (end point 1)
    */

    private static final Log logger = LogFactory.getLog(ResponseTemplate.class);

    private static String testUrlFormat = "http://%s:%s/messagebus/queues/%s/messages?appkey=%s&type=%s&timeout=%s";
    private static String testHost      = "localhost";
    private static int    testPort      = 8081;
    private static String testQueue     = "crm";
    private static String appkey        = "jahksjdfhakjdflkasdjflk";
    private static long   timeout       = 30000;

    private static volatile Object lockObj = new Object();

    public static void main(String[] args) {
        try {
            EndPoint1 e1 = new EndPoint1();
            e1.start();

            TimeUnit.SECONDS.sleep(3);

            EndPoint2 e2 = new EndPoint2();
            e2.start();

            //block 30s
            TimeUnit.SECONDS.sleep(40);
        } catch (InterruptedException e) {

        }
    }

    private static class EndPoint1 extends Thread {

        @Override
        public void run() {
            CloseableHttpResponse response = null;
            try {
                String url = String.format(testUrlFormat, testHost, testPort, testQueue, appkey, "request", timeout);

                CloseableHttpClient httpClient = HttpClients.createDefault();

                Message testMsg = MessageFactory.createMessage(MessageType.QueueMessage);
                QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
                body.setContent("test".getBytes());
                testMsg.setMessageBody(body);

                String msg2json = MessageJSONSerializer.serialize(testMsg);

                HttpPost postRequest = new HttpPost(url);
                List<NameValuePair> nvps = new ArrayList<>();
                nvps.add(new BasicNameValuePair("message", msg2json));
                postRequest.setEntity(new UrlEncodedFormEntity(nvps));

                response = httpClient.execute(postRequest);

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    logger.info("end point 1 received response : " + EntityUtils.toString(entity));
                }
            } catch (IOException e) {
                logger.error("[syncHTTPGet] occurs a IOException : " + e.getMessage());
            } finally {
                if (response != null)
                    try {
                        response.close();
                    } catch (IOException e) {
                        logger.error("[syncHTTPGet] finally block occurs a IOException : " + e.getMessage());
                    }
            }
        }
    }

    private static class EndPoint2 extends Thread {

        @Override
        public void run() {
            CloseableHttpResponse response = null;
            CloseableHttpResponse resp = null;
            try {
                synchronized (lockObj) {
                    String url = String.format(testUrlFormat, testHost, testPort, testQueue, appkey, "consume", timeout);
                    url += "&mode=sync&num=1";

                    CloseableHttpClient httpClient = HttpClients.createDefault();
                    HttpGet get = new HttpGet(url);

                    //get request
                    response = httpClient.execute(get);
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        String responseData = EntityUtils.toString(entity);
                        logger.info("end point 2 : received response : " + responseData);

                        Message msg = extractRequestMsg(responseData);

                        String tmpQueueName = String.valueOf(msg.getMessageHeader().getMessageId());

                        String responseUrl = String.format(testUrlFormat, testHost, testPort,
                                                           tmpQueueName, appkey, "response", timeout);

                        //send response
                        CloseableHttpClient responseHttpClient = HttpClients.createDefault();


                        Message testMsg = MessageFactory.createMessage(MessageType.QueueMessage);
                        QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
                        body.setContent("test".getBytes());
                        testMsg.setMessageBody(body);

                        String msg2json = MessageJSONSerializer.serialize(testMsg);

                        HttpPost postRequest = new HttpPost(responseUrl);
                        List<NameValuePair> nvps = new ArrayList<>();
                        nvps.add(new BasicNameValuePair("message", msg2json));
                        postRequest.setEntity(new UrlEncodedFormEntity(nvps));

                        resp = responseHttpClient.execute(postRequest);
                        HttpEntity responseEntity = response.getEntity();

                        lockObj.notify();
                    }
                }
            } catch (IOException e) {
                logger.error("[syncHTTPGet] occurs a IOException : " + e.getMessage());
            } finally {
                if (response != null)
                    try {
                        response.close();
                    } catch (IOException e) {
                        logger.error("[syncHTTPGet] finally block occurs a IOException : " + e.getMessage());
                    }

                if (resp != null)
                    try {
                        resp.close();
                    } catch (IOException e) {
                        logger.error("[syncHTTPGet] finally block occurs a IOException : " + e.getMessage());
                    }
            }
        }
    }

    private static Message extractRequestMsg(String respData) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(respData);
        JsonObject object = element.getAsJsonObject();
        JsonElement dataElement = object.get("data");
        if (!dataElement.isJsonArray()) {
            return null;
        }

        JsonElement msgElement = dataElement.getAsJsonArray().get(0);

        return MessageJSONSerializer.deSerialize(msgElement, MessageType.QueueMessage);
    }


}
```

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

##licence
Copyright (c) 2014-2015 yanghua. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

```
http://www.apache.org/licenses/LICENSE-2.0
```
Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.



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
[14]:https://raw.githubusercontent.com/yanghua/banyan/master/screenshots/overview/rabbitmq-offical-screenshot.png