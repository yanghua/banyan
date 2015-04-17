package com.messagebus.client;

import com.messagebus.business.exchanger.ExchangerManager;
import com.messagebus.common.Constants;
import com.messagebus.common.RandomHelper;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.IOException;

/**
 * Created by yanghua on 3/18/15.
 */
public class MessagebusPool {

    private String           poolId;
    private String           pubsuberHost;
    private int              pubsuberPort;
    private ExchangerManager exchangeManager;
    private ConfigManager    configManager;
    private Connection       connection;

    protected InnerPool innerPool;

    public MessagebusPool(String pubsuberHost, int pubsuberPort, GenericObjectPoolConfig poolConfig) {
        this.pubsuberHost = pubsuberHost;
        this.pubsuberPort = pubsuberPort;
        this.init();
        this.innerPool = new InnerPool(poolConfig,
                                       pubsuberHost,
                                       pubsuberPort,
                                       exchangeManager,
                                       configManager,
                                       connection);
    }

    public Messagebus getResource() {
        return this.innerPool.getResource();
    }

    public void returnResource(Messagebus client) {
        this.innerPool.returnResource(client);
    }

    public void destroy() {
        this.innerPool.destroy();

        //release resource
        if (exchangeManager != null)
            exchangeManager.removeRegister(this.poolId);

        if (configManager != null)
            configManager.destroy();


        if (this.connection != null && this.connection.isOpen()) {
            try {
                this.connection.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void init() {
        this.exchangeManager = new ExchangerManager(this.pubsuberHost, this.pubsuberPort);

        if (!this.exchangeManager.isPubsuberAlive())
            throw new RuntimeException("can not connect to pubsub server.");

        this.configManager = new ConfigManager();
        this.configManager.setExchangeManager(this.exchangeManager);
        this.poolId = RandomHelper.randomNumberAndCharacter(12);
        this.exchangeManager.registerWithMultiChannels(poolId, this.configManager, new String[]{
            Constants.PUBSUB_ROUTER_CHANNEL,
            Constants.PUBSUB_CONFIG_CHANNEL,
            Constants.PUBSUB_EVENT_CHANNEL,
            Constants.PUBSUB_SINK_CHANNEL,
            Constants.PUBSUB_CHANNEL_CHANNEL,
        });

        try {
            this.configManager.parseRealTimeData();

            ConnectionFactory connectionFactory = new ConnectionFactory();
            String host = this.configManager.getClientConfigMap().get("messagebus.client.host").getValue();
            connectionFactory.setHost(host);

            if (this.configManager.getClientConfigMap().containsKey("messagebus.client.port")) {
                int port = Integer.parseInt(this.configManager.getClientConfigMap().get("messagebus.client.port").getValue().toString());
                connectionFactory.setPort(port);
            } else {
                connectionFactory.setPort(connectionFactory.DEFAULT_AMQP_PORT);
            }

            connectionFactory.setAutomaticRecoveryEnabled(true);
            connectionFactory.setTopologyRecoveryEnabled(true);
            connectionFactory.setConnectionTimeout(5000);
            connectionFactory.setRequestedHeartbeat(5);

            this.connection = connectionFactory.newConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
