##消息类型
参照jms规范，目前messagebus支持如下五种消息：

* Stream - 流
* Bytes - 字节数组
* Map - map(键值对)
* Object - 对象类型
* Text - 文本类型

继承关系如下图：

![img 1][1]


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


[1]:https://raw.githubusercontent.com/yanghua/messagebus/master/screenshots/message-inherits.png