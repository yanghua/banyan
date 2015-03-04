package com.freedom.messagebus.client;

import com.freedom.messagebus.business.exchanger.ExchangerManager;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.common.Constants;
import com.freedom.messagebus.common.ExceptionHelper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by yanghua on 3/1/15.
 */
abstract class InnerClient {

    private static final Log logger = LogFactory.getLog(Messagebus.class);

    private   ExchangerManager exchangeManager;
    private   ConfigManager    configManager;
    private   Connection       connection;
    private   String           appId;
    private   Channel          channel;
    protected GenericContext   context;
    private   String           pubsuberHost;
    private   int              pubsuberPort;

    private AtomicBoolean isOpen = new AtomicBoolean(false);

    public InnerClient(String appId) {
        this.appId = appId;
    }

    public void open() throws MessagebusConnectedFailedException {
        if (this.isOpen())
            return;

        this.exchangeManager = new ExchangerManager(this.getPubsuberHost(), this.getPubsuberPort());

        if (!this.exchangeManager.isZKAlive())
            throw new MessagebusConnectedFailedException("can not connect to zookeeper server.");

        this.configManager = new ConfigManager();
        this.configManager.setExchangeManager(this.exchangeManager);
        this.exchangeManager.registerWithMultiChannels(this.appId, this.configManager, new String[]{
            Constants.PUBSUB_ROUTER_CHANNEL,
            Constants.PUBSUB_CONFIG_CHANNEL,
            Constants.PUBSUB_EVENT_CHANNEL,
            Constants.PUBSUB_AUTH_CHANNEL,
            Constants.PUBSUB_AUTH_SEND_PERMISSION_CHANNEL,
            Constants.PUBSUB_AUTH_RECEIVE_PERMISSION_CHANNEL
        });

        try {
            this.configManager.parseRealTimeData();
        } catch (IOException e) {
            throw new MessagebusConnectedFailedException(e);
        }

        this.initConnection();

        boolean isAuthorized = this.doAuth();
        if (!isAuthorized)
            throw new PermissionException("the appId : " + this.appId + " is illegal.");

        context = new GenericContext();
        context.setAppId(appId);
        context.setChannel(this.channel);
        context.setConfigManager(this.configManager);
        context.setConnection(this.connection);

        boolean success = this.isOpen.compareAndSet(false, true);
        if (!success) {
            logger.error("occurs a non-consistency : the field isOpen should be false but it's actually true");
        }
    }

    public void close() {
        //release all resource
        try {
            if (this.exchangeManager != null)
                this.exchangeManager.removeRegister(this.appId);

            if (this.configManager != null)
                this.configManager.destroy();

            if (this.channel != null && this.channel.isOpen())
                this.channel.close();

            if (this.connection != null && this.connection.isOpen())
                this.connection.close();

            this.isOpen.compareAndSet(true, false);
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "close");
            throw new RuntimeException(e);
        }
    }

    public boolean isOpen() {
        return this.isOpen.get();
    }

    public String getPubsuberHost() {
        if (this.pubsuberHost == null || this.pubsuberHost.isEmpty())
            this.pubsuberHost = "localhost";

        return pubsuberHost;
    }

    public void setPubsuberHost(String pubsuberHost) {
        this.pubsuberHost = pubsuberHost;
    }

    public int getPubsuberPort() {
        if (this.pubsuberPort == 0) {
            throw new RuntimeException("pubsuber port error!");
        }

        return pubsuberPort;
    }

    public void setPubsuberPort(int pubsuberPort) {
        this.pubsuberPort = pubsuberPort;
    }

    public String getAppId() {
        return appId;
    }

    private void initConnection() throws MessagebusConnectedFailedException {
        try {
            String host = this.configManager.getClientConfigMap().get("messagebus.client.host").getValue();

            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(host);

            this.connection = connectionFactory.newConnection();
            this.channel = this.connection.createChannel();
        } catch (IOException e) {
            throw new MessagebusConnectedFailedException(e);
        }
    }

    private boolean doAuth() {
        return this.configManager.getAppIdQueueMap().containsKey(this.appId);
    }

}
