package com.freedom.messagebus.manager;

import com.freedom.messagebus.common.RouterType;
import com.rabbitmq.client.Channel;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * User: yanghua
 * Date: 7/18/14
 * Time: 4:24 PM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public class TopologyManager {

    @NotNull
    private Channel channel;
    private static boolean isInited = false;
    private static volatile TopologyManager instance;

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

    private TopologyManager(@NotNull Channel channel) {
        this.channel = channel;
    }

    public static TopologyManager getInstance(@NotNull Channel channel) {
        if (instance == null) {
            synchronized (TopologyManager.class) {
                if (instance == null) {
                    instance = new TopologyManager(channel);
                    isInited = true;
                }
            }
        }

        return instance;
    }

    //region init flow
    public void init() throws IOException {
        if (!this.channel.isOpen())
            throw new IllegalStateException("[init] : the channel is closed");
        this.declareRouter();
        this.bindRouter();
        this.declareQueue();
        this.bindQueue();
    }

    //region destroy flow
    public void destroy() throws IOException {
        if (!isInited)
            throw new IllegalStateException("[destroy] : illegal state! ");

        if (!this.channel.isOpen())
            throw new IllegalStateException("[destroy] : the channel is closed");

        this.unBindQueue();
        this.deleteQueue();
        this.unBindRouter();
        this.deleteRouter();

        isInited = false;
        //TODO: release channel
    }

    private void declareRouter() throws IOException {
        //first level
        this.channel.exchangeDeclare(this.proxyExchangeName, this.proxyExchangeType.toString(), true);
        //second level
        this.channel.exchangeDeclare(this.logExchangeName, this.logExchangeType.toString(), true);
        this.channel.exchangeDeclare(this.messageExchangeName, this.messageExchangeType.toString(), true);
        //third level
        this.channel.exchangeDeclare(this.systemExchangeName, this.systemExchangeType.toString(), true);
        this.channel.exchangeDeclare(this.businessExchangeName, this.businessExchangeType.toString(), true);
        //forth level
        this.channel.exchangeDeclare(this.oaExchangeName, this.oaExchangeType.toString(), true);
    }

    private void bindRouter() throws IOException {
        //second level bind to first level
        this.channel.exchangeBind(this.logExchangeName, this.proxyExchangeName, "");
        this.channel.exchangeBind(this.messageExchangeName, this.proxyExchangeName, "");
        //third level bind to second level
        this.channel.exchangeBind(this.systemExchangeName, this.messageExchangeName, this.routingKeyForSystem);
        this.channel.exchangeBind(this.businessExchangeName, this.messageExchangeName, this.routingKeyForBusiness);
        //forth level bind to third level
        this.channel.exchangeBind(this.oaExchangeName, this.businessExchangeName, this.oaRoutingKey);
    }

    private void declareQueue() throws IOException {
        //log exchange's queues
        this.channel.queueDeclare(this.consoleQueueName, true, false, false, null);
        this.channel.queueDeclare(this.fileQueueName, true, false, false, null);

        //system exchange's queues
        this.channel.queueDeclare(this.notificationQueueName, true, false, false, null);
        this.channel.queueDeclare(this.reportStateQueueName, true, false, false, null);

        //business exchange's queue
        this.channel.queueDeclare(this.erpQueueName, true, false, false, null);
        this.channel.queueDeclare(this.crmQueueName, true, false, false, null);

        //oa exchange (under business exchange) queue
        this.channel.queueDeclare(this.oaSmsQueueName, true, false, false, null);
        this.channel.queueDeclare(this.oaEmailQueueName, true, false, false, null);
    }

    private void bindQueue() throws IOException {
        this.channel.queueBind(this.consoleQueueName, this.logExchangeName, "", null);
        this.channel.queueBind(this.fileQueueName, this.logExchangeName, "", null);

        this.channel.queueBind(this.notificationQueueName, this.systemExchangeName, this.sysNotificationRoutingKey, null);
        this.channel.queueBind(this.reportStateQueueName, this.systemExchangeName, this.sysReportStateRoutingKey, null);

        this.channel.queueBind(this.erpQueueName, this.businessExchangeName, this.erpRoutingKey, null);
        this.channel.queueBind(this.crmQueueName, this.businessExchangeName, this.crmRoutingKey, null);

        this.channel.queueBind(this.oaSmsQueueName, this.oaExchangeName, this.oaSmsRoutingKey, null);
        this.channel.queueBind(this.oaEmailQueueName, this.oaExchangeName, this.oaEmailRoutingKey, null);
    }
    //endregion


    private void unBindQueue() throws IOException {
        this.channel.queueUnbind(this.consoleQueueName, this.logExchangeName, "", null);
        this.channel.queueUnbind(this.fileQueueName, this.logExchangeName, "", null);

        this.channel.queueUnbind(this.notificationQueueName, this.systemExchangeName, "", null);
        this.channel.queueUnbind(this.reportStateQueueName, this.systemExchangeName, "", null);

        this.channel.queueUnbind(this.erpQueueName, this.businessExchangeName, this.erpRoutingKey, null);
        this.channel.queueUnbind(this.crmQueueName, this.businessExchangeName, this.crmRoutingKey, null);

        this.channel.queueUnbind(this.oaSmsQueueName, this.oaExchangeName, this.oaSmsRoutingKey, null);
        this.channel.queueUnbind(this.oaEmailQueueName, this.oaEmailQueueName, this.oaEmailRoutingKey, null);
    }

    private void deleteQueue() throws IOException {
        this.channel.queueDelete(this.consoleQueueName);
        this.channel.queueDelete(this.fileQueueName);

        this.channel.queueDelete(this.notificationQueueName);
        this.channel.queueDelete(this.reportStateQueueName);

        this.channel.queueDelete(this.erpQueueName);
        this.channel.queueDelete(this.crmQueueName);

        this.channel.queueDelete(this.oaSmsQueueName);
        this.channel.queueDelete(this.oaEmailQueueName);
    }

    private void unBindRouter() throws IOException {
        this.channel.exchangeUnbind(this.logExchangeName, this.proxyExchangeName, "");
        this.channel.exchangeUnbind(this.messageExchangeName, this.proxyExchangeName, "");

        this.channel.exchangeBind(this.systemExchangeName, this.messageExchangeName, this.routingKeyForSystem);
        this.channel.exchangeBind(this.businessExchangeName, this.messageExchangeName, this.routingKeyForBusiness);

        this.channel.exchangeUnbind(this.oaExchangeName, this.businessExchangeName, this.oaRoutingKey);
    }

    private void deleteRouter() throws IOException {
        this.channel.exchangeDelete(this.proxyExchangeName);

        this.channel.exchangeDelete(this.logExchangeName);
        this.channel.exchangeDelete(this.messageExchangeName);

        this.channel.exchangeDelete(this.systemExchangeName);
        this.channel.exchangeDelete(this.businessExchangeName);

        this.channel.exchangeDelete(this.oaExchangeName);
    }
    //endregion

}
