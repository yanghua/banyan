package com.freedom.messagebus.client;

import com.freedom.messagebus.common.RouterType;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import junit.framework.TestCase;

/**
 * Desc: this is a implementation of rabbitmq routing-topology by joining
 * some kind of exchange-types and queue.
 * User: yanghua
 * Date: 7/7/14
 * Time: 2:57 PM
 * Copyright (c) 2014 yanghua. All rights reserved.
 */
public class TestRouteTopology extends TestCase {

    protected ConnectionFactory connectionFactory;
    protected Connection        produceConnection;
    protected Channel           produceChannel;
    protected Connection        consumerConnection;
    protected Channel           consumerChannel;

    private String host = "";
    private Gson   gson = new Gson();

    //region exchange definition
    //proxy exchange
    private String     proxyExchangeName = "exchange.proxy";
    private RouterType proxyExchangeType = RouterType.FANOUT;

    //log exchange
    private String     logExchangeName = "exchange.proxy.log";
    private RouterType logExchangeType = RouterType.FANOUT;

    //message exchange
    private String     messageExchangeName = "exchange.proxy.message";
    private RouterType messageExchangeType = RouterType.TOPIC;

    //system exchange
    private String     systemExchangeName = "exchange.proxy.message.system";
    private RouterType systemExchangeType = RouterType.TOPIC;

    //business exchange
    private String     businessExchangeName = "exchange.proxy.message.business";
    private RouterType businessExchangeType = RouterType.TOPIC;

    private String     oaExchangeName = "exchange.proxy.message.business.oa";
    private RouterType oaExchangeType = RouterType.DIRECT;
    //endregion

    //region routing key definition
    //based on message for system and business
    private String routingKeyForSystem   = "routingkey.message.system.#";
    private String routingKeyForBusiness = "routingkey.message.business.#";

    //based on system for notification
    private String sysNotificationRoutingKey = "routingkey.message.system.notification";
    private String sysReportStateRoutingKey  = "routingkey.message.system.reportstate";

    //based on business for every registered app
    private String oaRoutingKey  = "routingkey.message.business.oa.#";
    private String erpRoutingKey = "routingkey.message.business.erp";
    private String crmRoutingKey = "routingkey.message.business.crm";

    private String oaSmsRoutingKey   = "routingkey.message.business.oa.sms";
    private String oaEmailRoutingKey = "routingkey.message.business.oa.email";
    //endregion

    //region queue definition
    private String consoleQueueName = "queue.proxy.log.console";
    private String fileQueueName    = "queue.proxy.log.file";

    private String notificationQueueName = "queue.proxy.message.system.notification";
    private String reportStateQueueName  = "queue.proxy.message.system.reportState";

    private String erpQueueName = "queue.proxy.message.business.erp";
    private String crmQueueName = "queue.proxy.message.business.crm";

    private String oaSmsQueueName   = "queue.proxy.message.business.oa.sms";
    private String oaEmailQueueName = "queue.proxy.message.business.oa.email";
    //endregion


    public void setUp() throws Exception {
        this.init();
        this.declareExchange();
        this.bindExchange();
        this.declareQueue();
        this.bindQueue();
    }

    public void tearDown() throws Exception {
        //wait for consumer finish!!!
        Thread.sleep(30000);

        this.unbindExchange();
        this.deleteExchange();
        this.unbindQueue();
        this.deleteQueue();

        this.consumerChannel.close();
        this.consumerConnection.close();
    }

    private void init() throws Exception {
        this.connectionFactory = new ConnectionFactory();
        this.connectionFactory.setHost(this.host);

        this.produceConnection = this.connectionFactory.newConnection();
        this.produceChannel = this.produceConnection.createChannel();

        this.consumerConnection = this.connectionFactory.newConnection();
        this.consumerChannel = this.consumerConnection.createChannel();
    }

    private void declareExchange() throws Exception {
        //first level
        this.produceChannel.exchangeDeclare(this.proxyExchangeName, this.proxyExchangeType.toString(), true);
        //second level
        this.produceChannel.exchangeDeclare(this.logExchangeName, this.logExchangeType.toString(), true);
        this.produceChannel.exchangeDeclare(this.messageExchangeName, this.messageExchangeType.toString(), true);
        //third level
        this.produceChannel.exchangeDeclare(this.systemExchangeName, this.systemExchangeType.toString(), true);
        this.produceChannel.exchangeDeclare(this.businessExchangeName, this.businessExchangeType.toString(), true);
        //forth level
        this.produceChannel.exchangeDeclare(this.oaExchangeName, this.oaExchangeType.toString(), true);
    }

    private void bindExchange() throws Exception {
        //second level bind to first level
        this.produceChannel.exchangeBind(this.logExchangeName, this.proxyExchangeName, "");
        this.produceChannel.exchangeBind(this.messageExchangeName, this.proxyExchangeName, "");
        //third level bind to second level
        this.produceChannel.exchangeBind(this.systemExchangeName, this.messageExchangeName, this.routingKeyForSystem);
        this.produceChannel.exchangeBind(this.businessExchangeName, this.messageExchangeName, this.routingKeyForBusiness);
        //forth level bind to third level
        this.produceChannel.exchangeBind(this.oaExchangeName, this.businessExchangeName, this.oaRoutingKey);
    }

    private void declareQueue() throws Exception {
        //log exchange's queues
        this.produceChannel.queueDeclare(this.consoleQueueName, true, false, false, null);
        this.produceChannel.queueDeclare(this.fileQueueName, true, false, false, null);

        //system exchange's queues
        this.produceChannel.queueDeclare(this.notificationQueueName, true, false, false, null);
        this.produceChannel.queueDeclare(this.reportStateQueueName, true, false, false, null);

        //business exchange's queue
        this.produceChannel.queueDeclare(this.erpQueueName, true, false, false, null);
        this.produceChannel.queueDeclare(this.crmQueueName, true, false, false, null);

        //oa exchange (under business exchange) queue
        this.produceChannel.queueDeclare(this.oaSmsQueueName, true, false, false, null);
        this.produceChannel.queueDeclare(this.oaEmailQueueName, true, false, false, null);
    }

    private void bindQueue() throws Exception {
        this.produceChannel.queueBind(this.consoleQueueName, this.logExchangeName, "", null);
        this.produceChannel.queueBind(this.fileQueueName, this.logExchangeName, "", null);

        this.produceChannel.queueBind(this.notificationQueueName, this.systemExchangeName, this.sysNotificationRoutingKey, null);
        this.produceChannel.queueBind(this.reportStateQueueName, this.systemExchangeName, this.sysReportStateRoutingKey, null);

        this.produceChannel.queueBind(this.erpQueueName, this.businessExchangeName, this.erpRoutingKey, null);
        this.produceChannel.queueBind(this.crmQueueName, this.businessExchangeName, this.crmRoutingKey, null);

        this.produceChannel.queueBind(this.oaSmsQueueName, this.oaExchangeName, this.oaSmsRoutingKey, null);
        this.produceChannel.queueBind(this.oaEmailQueueName, this.oaExchangeName, this.oaEmailRoutingKey, null);
    }

    private void unbindExchange() throws Exception {
        this.consumerChannel.exchangeUnbind(this.logExchangeName, this.proxyExchangeName, "");
        this.consumerChannel.exchangeUnbind(this.messageExchangeName, this.proxyExchangeName, "");

        this.consumerChannel.exchangeBind(this.systemExchangeName, this.messageExchangeName, this.routingKeyForSystem);
        this.consumerChannel.exchangeBind(this.businessExchangeName, this.messageExchangeName, this.routingKeyForBusiness);

        this.consumerChannel.exchangeUnbind(this.oaExchangeName, this.businessExchangeName, this.oaRoutingKey);
    }

    private void unbindQueue() throws Exception {
        this.consumerChannel.queueUnbind(this.consoleQueueName, this.logExchangeName, "", null);
        this.consumerChannel.queueUnbind(this.fileQueueName, this.logExchangeName, "", null);

        this.consumerChannel.queueUnbind(this.notificationQueueName, this.systemExchangeName, "", null);
        this.consumerChannel.queueUnbind(this.reportStateQueueName, this.systemExchangeName, "", null);

        this.consumerChannel.queueUnbind(this.erpQueueName, this.businessExchangeName, this.erpRoutingKey, null);
        this.consumerChannel.queueUnbind(this.crmQueueName, this.businessExchangeName, this.crmRoutingKey, null);

        this.consumerChannel.queueUnbind(this.oaSmsQueueName, this.oaExchangeName, this.oaSmsRoutingKey, null);
        this.consumerChannel.queueUnbind(this.oaEmailQueueName, this.oaEmailQueueName, this.oaEmailRoutingKey, null);
    }

    private void deleteExchange() throws Exception {
        this.consumerChannel.exchangeDelete(this.proxyExchangeName);

        this.consumerChannel.exchangeDelete(this.logExchangeName);
        this.consumerChannel.exchangeDelete(this.messageExchangeName);

        this.consumerChannel.exchangeDelete(this.systemExchangeName);
        this.consumerChannel.exchangeDelete(this.businessExchangeName);

        this.consumerChannel.exchangeDelete(this.oaExchangeName);
    }

    private void deleteQueue() throws Exception {
        this.consumerChannel.queueDelete(this.consoleQueueName);
        this.consumerChannel.queueDelete(this.fileQueueName);

        this.consumerChannel.queueDelete(this.notificationQueueName);
        this.consumerChannel.queueDelete(this.reportStateQueueName);

        this.consumerChannel.queueDelete(this.erpQueueName);
        this.consumerChannel.queueDelete(this.crmQueueName);

        this.consumerChannel.queueDelete(this.oaSmsQueueName);
        this.consumerChannel.queueDelete(this.oaEmailQueueName);
    }

//    public void testRouteToLogExchange() throws Exception {
//        QueueingConsumer c1 = new QueueingConsumer(this.consumerChannel);
//        this.consumerChannel.basicConsume(this.consoleQueueName, false, c1);
//        SimpleMsgReceiverForTest r1 = new SimpleMsgReceiverForTest(true, false, false);
//        r1.start(c1, this.consumerChannel, null);
//
//        QueueingConsumer c2 = new QueueingConsumer(this.consumerChannel);
//        this.consumerChannel.basicConsume(this.fileQueueName, false, c2);
//        SimpleMsgReceiverForTest r2 = new SimpleMsgReceiverForTest(true, false, false);
//        r2.start(c2, this.consumerChannel, null);
//
//        Message msg = new Message();
//        msg.setMessageBody("test console queue");
//        String msgJsonStr = gson.toJson(msg);
//
//        this.produceChannel.basicPublish(this.proxyExchangeName, "", null, msgJsonStr.getBytes());
//
//        this.produceChannel.close();
//        this.produceConnection.close();
//    }
//
//    public void testRouteToSysExchange() throws Exception {
//        QueueingConsumer c1 = new QueueingConsumer(this.consumerChannel);
//        this.consumerChannel.basicConsume(this.notificationQueueName, false, c1);
//        SimpleMsgReceiverForTest r1 = new SimpleMsgReceiverForTest(true, false, false);
//        r1.start(c1, this.consumerChannel, null);
//
//        QueueingConsumer c2 = new QueueingConsumer(this.consumerChannel);
//        this.consumerChannel.basicConsume(this.reportStateQueueName, false, c2);
//        SimpleMsgReceiverForTest r2 = new SimpleMsgReceiverForTest(true, false, false);
//        r2.start(c2, this.consumerChannel, null);
//
//        Message msg = new Message();
//        msg.setMessageBody("test receive from system queue");
//        String msgJsonStr = gson.toJson(msg);
//
//        this.produceChannel.basicPublish(this.proxyExchangeName, this.sysNotificationRoutingKey, null, msgJsonStr.getBytes());
//        this.produceChannel.basicPublish(this.proxyExchangeName, this.sysReportStateRoutingKey, null, msgJsonStr.getBytes());
//
//        this.produceChannel.close();
//        this.produceConnection.close();
//    }
//
//    public void testRouteToBusinessExchange() throws Exception {
//        QueueingConsumer c1 = new QueueingConsumer(this.consumerChannel);
//        this.consumerChannel.basicConsume(this.erpQueueName, false, c1);
//        SimpleMsgReceiverForTest r2 = new SimpleMsgReceiverForTest(true, false, false);
//        r2.start(c1, this.consumerChannel, null);
//
//        QueueingConsumer c2 = new QueueingConsumer(this.consumerChannel);
//        this.consumerChannel.basicConsume(this.crmQueueName, false, c2);
//        SimpleMsgReceiverForTest r3 = new SimpleMsgReceiverForTest(true, false, false);
//        r3.start(c2, this.consumerChannel, null);
//
//        Message msg = new Message();
//        msg.setMessageBody("test receive from business queue");
//        String msgJsonStr = gson.toJson(msg);
//
//        this.produceChannel.basicPublish(this.proxyExchangeName, this.crmRoutingKey, null, msgJsonStr.getBytes());
//        this.produceChannel.basicPublish(this.proxyExchangeName, this.erpRoutingKey, null, msgJsonStr.getBytes());
//
//        this.produceChannel.close();
//        this.produceConnection.close();
//    }
//
//    public void testRouteToOAExchange() throws Exception {
//        QueueingConsumer c1 = new QueueingConsumer(this.consumerChannel);
//        this.consumerChannel.basicConsume(this.oaSmsQueueName, false, c1);
//        SimpleMsgReceiverForTest r1 = new SimpleMsgReceiverForTest(true, false, false);
//        r1.start(c1, this.consumerChannel, null);
//
//        QueueingConsumer c2 = new QueueingConsumer(this.consumerChannel);
//        this.consumerChannel.basicConsume(this.oaEmailQueueName, false, c2);
//        SimpleMsgReceiverForTest r2 = new SimpleMsgReceiverForTest(true, false, false);
//        r2.start(c2, this.consumerChannel, null);
//
//        Message msg = new Message();
//        msg.setMessageBody("test receive from oa queue");
//        String msgJsonStr = gson.toJson(msg);
//
//        this.produceChannel.basicPublish(this.proxyExchangeName, this.oaSmsRoutingKey, null, msgJsonStr.getBytes());
//        this.produceChannel.basicPublish(this.proxyExchangeName, this.oaEmailRoutingKey, null, msgJsonStr.getBytes());
//
//        this.produceChannel.close();
//        this.produceConnection.close();
//    }
//
//    public void testRouteThroughFullTopology() throws Exception {
//        Message msg = new Message();
//        String msgJsonStr = "";
//
//        //common log consumer - console
//        QueueingConsumer commonConsoleLogConsumer = new QueueingConsumer(this.consumerChannel);
//        this.consumerChannel.basicConsume(this.consoleQueueName, false, commonConsoleLogConsumer);
//        SimpleMsgReceiverForTest commonConsoleLogReceiver = new SimpleMsgReceiverForTest(true, false, false);
//        commonConsoleLogReceiver.start(commonConsoleLogConsumer, this.consumerChannel, null);
//
//        //common log consumer - file
//        QueueingConsumer commonFileLogConsumer = new QueueingConsumer(this.consumerChannel);
//        this.consumerChannel.basicConsume(this.fileQueueName, false, commonConsoleLogConsumer);
//        SimpleMsgReceiverForTest commonFileLogReceiver = new SimpleMsgReceiverForTest(true, false, false);
//        commonFileLogReceiver.start(commonFileLogConsumer, this.consumerChannel, null);
//
//        //oa consumer
//        QueueingConsumer smsConsumer = new QueueingConsumer(this.consumerChannel);
//        this.consumerChannel.basicConsume(this.oaSmsQueueName, false, smsConsumer);
//        SimpleMsgReceiverForTest smsReceiver = new SimpleMsgReceiverForTest(true, false, false);
//        smsReceiver.start(smsConsumer, this.consumerChannel, null);
//
//        msg.setMessageBody("test business queue");
//        msg.setAppKeyFrom("oa - sms");
//        msgJsonStr = gson.toJson(msg);
//        this.produceChannel.basicPublish(this.proxyExchangeName, this.oaSmsRoutingKey, null, msgJsonStr.getBytes());
//        //just for watching the result (console and rabbitmq's web dashboard) and the same below
//        Thread.sleep(10000);
//
//        QueueingConsumer emailConsumer = new QueueingConsumer(this.consumerChannel);
//        this.consumerChannel.basicConsume(this.oaEmailQueueName, false, emailConsumer);
//        SimpleMsgReceiverForTest emailReceiver = new SimpleMsgReceiverForTest(true, false, false);
//        emailReceiver.start(emailConsumer, this.consumerChannel, null);
//
//        msg.setMessageBody("test business queue");
//        msg.setAppKeyFrom("oa - email");
//        msgJsonStr = gson.toJson(msg);
//        this.produceChannel.basicPublish(this.proxyExchangeName, this.oaEmailRoutingKey, null, msgJsonStr.getBytes());
//        Thread.sleep(10000);
//
//        //erp consumer
//        QueueingConsumer erpConsumer = new QueueingConsumer(this.consumerChannel);
//        this.consumerChannel.basicConsume(this.erpQueueName, false, erpConsumer);
//        SimpleMsgReceiverForTest erpReceiver = new SimpleMsgReceiverForTest(true, false, false);
//        erpReceiver.start(erpConsumer, this.consumerChannel, null);
//
//        msg.setMessageBody("test business queue");
//        msg.setAppKeyFrom("erp");
//        msgJsonStr = gson.toJson(msg);
//        this.produceChannel.basicPublish(this.proxyExchangeName, this.erpRoutingKey, null, msgJsonStr.getBytes());
//        Thread.sleep(10000);
//
//        //crm consumer
//        QueueingConsumer crmConsumer = new QueueingConsumer(this.consumerChannel);
//        this.consumerChannel.basicConsume(this.crmQueueName, false, crmConsumer);
//        SimpleMsgReceiverForTest crmReceiver = new SimpleMsgReceiverForTest(true, false, false);
//        crmReceiver.start(crmConsumer, this.consumerChannel, null);
//
//        msg.setMessageBody("test business queue");
//        msg.setAppKeyFrom("crm");
//        msgJsonStr = gson.toJson(msg);
//        this.produceChannel.basicPublish(this.proxyExchangeName, this.crmRoutingKey, null, msgJsonStr.getBytes());
//        Thread.sleep(10000);
//
//        //reportState consumer
//        QueueingConsumer reportStateConsumer = new QueueingConsumer(this.consumerChannel);
//        this.consumerChannel.basicConsume(this.reportStateQueueName, false, reportStateConsumer);
//        SimpleMsgReceiverForTest reportStateReceiver = new SimpleMsgReceiverForTest(true, false, false);
//        reportStateReceiver.start(reportStateConsumer, this.consumerChannel, null);
//
//        msg.setMessageBody("test system queue");
//        msg.setAppKeyFrom("reportState");
//        msgJsonStr = gson.toJson(msg);
//        this.produceChannel.basicPublish(this.proxyExchangeName, this.sysNotificationRoutingKey, null, msgJsonStr.getBytes());
//        Thread.sleep(10000);
//
//        //notification consumer
//        QueueingConsumer notificationConsumer = new QueueingConsumer(this.consumerChannel);
//        this.consumerChannel.basicConsume(this.notificationQueueName, false, notificationConsumer);
//        SimpleMsgReceiverForTest notificationReceiver = new SimpleMsgReceiverForTest(true, false, false);
//        notificationReceiver.start(notificationConsumer, this.consumerChannel, null);
//
//        msg.setMessageBody("test system queue");
//        msg.setAppKeyFrom("notification");
//        msgJsonStr = gson.toJson(msg);
//        this.produceChannel.basicPublish(this.proxyExchangeName, this.sysNotificationRoutingKey, null, msgJsonStr.getBytes());
//        Thread.sleep(10000);
//
//        this.produceChannel.close();
//        this.produceConnection.close();
//    }

}