package com.freedom.messagebus.client;

import com.freedom.messagebus.client.core.message.Message;
import com.freedom.messagebus.client.core.message.ObjectMessage;
import com.freedom.messagebus.client.core.message.TextMessage;
import com.freedom.messagebus.client.model.MessageFormat;
import com.freedom.messagebus.client.model.message.ObjectMessagePOJO;
import com.freedom.messagebus.client.model.message.TextMessagePOJO;
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


    private Messagebus client;

    private String appkey;
    private String msgType;

    public void setUp() throws Exception {
        client = Messagebus.getInstance();
        client.setZkHost("localhost");
        client.setZkPort(2181);
        client.open();
    }

    public void tearDown() throws Exception {

    }

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

    public void testBatchProduceAndConsume() throws Exception {
        //start consume
        appkey = java.util.UUID.randomUUID().toString();
        msgType = "business";
        String queueName = "oa.email";
        IConsumerCloser closer = client.getConsumer().consume(appkey, msgType, queueName, new IMessageReceiveListener() {
            @Override
            public void onMessage(Message msg, MessageFormat format) {
                switch (format) {
                    case Text: {
                        TextMessage txtMsg = (TextMessage) msg;
                        logger.debug("received message : " + txtMsg.getMessageBody());
                    }
                    break;

                }
            }
        });

        //start produce
        TextMessagePOJO msg1 = new TextMessagePOJO();
        msg1.setMessageBody("just a test 1");

        TextMessagePOJO msg2 = new TextMessagePOJO();
        msg1.setMessageBody("just a test 2");

        Message[] msgArr = new Message[]{msg1, msg2};

        client.getProducer().batchProduce(msgArr, MessageFormat.Text, appkey, queueName, msgType);

        //sleep for checking the result
        Thread.sleep(10000);
        closer.closeConsumer();
    }

    public void testSimpleProduceAndConsumeWithTX() throws Exception {
        //start consume
        appkey = java.util.UUID.randomUUID().toString();
        msgType = "business";
        String queueName = "erp";
        IConsumerCloser closer = client.getConsumer().consume(appkey, msgType, queueName, new IMessageReceiveListener() {
            @Override
            public void onMessage(Message msg, MessageFormat format) {
                switch (format) {
                    case Text: {
                        TextMessage txtMsg = (TextMessage) msg;
                        logger.debug("received message : " + txtMsg.getMessageBody());
                    }
                    break;

                }
            }
        });

        //start produce
        TextMessagePOJO msg = new TextMessagePOJO();
        msg.setMessageBody("just a test");
        client.getProducer().produceWithTX(msg, MessageFormat.Text, appkey, queueName, msgType);

        //sleep for checking the result
        Thread.sleep(10000);
        closer.closeConsumer();

    }

    public void testBatchProduceAndConsumeWithTX() throws Exception {
        //start consume
        appkey = java.util.UUID.randomUUID().toString();
        msgType = "business";
        String queueName = "crm";
        IConsumerCloser closer = client.getConsumer().consume(appkey, msgType, queueName, new IMessageReceiveListener() {
            @Override
            public void onMessage(Message msg, MessageFormat format) {
                switch (format) {
                    case Text: {
                        TextMessage txtMsg = (TextMessage) msg;
                        logger.debug("received message : " + txtMsg.getMessageBody());
                    }
                    break;

                }
            }
        });

        //start produce
        TextMessagePOJO msg1 = new TextMessagePOJO();
        msg1.setMessageBody("just a test 1");

        TextMessagePOJO msg2 = new TextMessagePOJO();
        msg2.setMessageBody("just a test 2");

        Message[] msgArr = new Message[]{msg1, msg2};

        client.getProducer().batchProduceWithTX(msgArr, MessageFormat.Text, appkey, queueName, msgType);

        //sleep for checking the result
        Thread.sleep(10000);
        closer.closeConsumer();
    }
}
