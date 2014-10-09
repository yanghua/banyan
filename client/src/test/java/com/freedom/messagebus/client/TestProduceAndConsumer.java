package com.freedom.messagebus.client;

import com.freedom.messagebus.client.core.config.LongLiveZookeeper;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.common.message.MessageFactory;
import com.freedom.messagebus.common.message.MessageType;
import com.freedom.messagebus.common.message.messageBody.AppMessageBody;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * User: yanghua
 * Date: 7/21/14
 * Time: 10:31 AM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public class TestProduceAndConsumer extends TestCase {

    private static final Log logger = LogFactory.getLog(TestProduceAndConsumer.class);

    private LongLiveZookeeper zooKeeper;
    private Messagebus        client;

    private String appkey;
    private String msgType;

    private String host = "115.29.96.85";
    private int    port = 2181;

    public void setUp() throws Exception {
        appkey = "kobe";
        client = Messagebus.getInstance(appkey);
        client.setZkHost(host);
        client.setZkPort(port);
        client.open();
        zooKeeper = LongLiveZookeeper.getZKInstance(host, port);
    }

    public void tearDown() throws Exception {

    }

    public void testSimpleProduceAndConsume() throws Exception {
        //start consume
        String queueName = "crm";
        IConsumerCloser closer = client.getConsumer().consume(queueName,
                                                              new IMessageReceiveListener() {

                                                                  @Override
                                                                  public void onMessage(Message message, IConsumerCloser consumerCloser) {
                                                                      logger.info("[message-id] : " + message.getMessageHeader().getMessageId());
                                                                  }
                                                              });

        Message msg = MessageFactory.createMessage(MessageType.AppMessage);

        AppMessageBody appMessageBody = (AppMessageBody) msg.getMessageBody();
        appMessageBody.setMessageBody("test".getBytes());

        client.getProducer().produce(msg, queueName);

        //sleep for checking the result
        Thread.sleep(600000);

        closer.closeConsumer();

        client.close();
    }

//    public void testBatchProduceAndConsume() throws Exception {
//        //start consume
//        appkey = java.util.UUID.randomUUID().toString();
//        msgType = "business";
//        String queueName = "oa.email";
//        IConsumerCloser closer = client.getConsumer().consume(appkey, msgType, queueName,
//                                                              new IMessageReceiveListener() {
//
//                                                                  @Override
//                                                                  public void onMessage(Message message, MessageType messageType) {
//
//                                                                  }
//                                                              });
//
//        //start produce
//        TextMessagePOJO msg1 = new TextMessagePOJO();
//        msg1.setMessageBody("just a test 1");
//
//        TextMessagePOJO msg2 = new TextMessagePOJO();
//        msg1.setMessageBody("just a test 2");
//
////        Message[] msgArr = new Message[]{msg1, msg2};
////
////        client.getProducer().batchProduce(msgArr, MessageFormat.Text, appkey, queueName, msgType);
//
//        //sleep for checking the result
//        Thread.sleep(10000);
//        closer.closeConsumer();
//    }
//
//    public void testSimpleProduceAndConsumeWithTX() throws Exception {
//        //start consume
//        appkey = java.util.UUID.randomUUID().toString();
//        msgType = "business";
//        String queueName = "erp";
//        IConsumerCloser closer = client.getConsumer().consume(appkey, msgType, queueName, new IMessageReceiveListener() {
//
//            @Override
//            public void onMessage(Message message, MessageType messageType) {
//
//            }
//        });
//
//        //start produce
//        TextMessagePOJO msg = new TextMessagePOJO();
//        msg.setMessageBody("just a test");
//        client.getProducer().produceWithTX(msg, MessageFormat.Text, appkey, queueName, msgType);
//
//        //sleep for checking the result
//        Thread.sleep(10000);
//        closer.closeConsumer();
//
//    }
//
//    public void testBatchProduceAndConsumeWithTX() throws Exception {
//        //start consume
//        appkey = java.util.UUID.randomUUID().toString();
//        msgType = "business";
//        String queueName = "crm";
//        IConsumerCloser closer = client.getConsumer().consume(appkey, msgType, queueName, new IMessageReceiveListener() {
//
//            @Override
//            public void onMessage(Message message, MessageType messageType) {
//
//            }
//        });
//
//        //start produce
//        TextMessagePOJO msg1 = new TextMessagePOJO();
//        msg1.setMessageBody("just a test 1");
//
//        TextMessagePOJO msg2 = new TextMessagePOJO();
//        msg2.setMessageBody("just a test 2");
//
////        Message[] msgArr = new Message[]{msg1, msg2};
////
////        client.getProducer().batchProduceWithTX(msgArr, MessageFormat.Text, appkey, queueName, msgType);
//
//        //sleep for checking the result
//        Thread.sleep(10000);
//        closer.closeConsumer();
//    }
}
