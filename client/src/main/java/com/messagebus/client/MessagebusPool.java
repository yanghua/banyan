package com.messagebus.client;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.messagebus.client.event.component.ClientDestroyEventProcessor;
import com.messagebus.client.event.component.ClientInitedEventProcessor;
import com.messagebus.common.Constants;
import com.messagebus.interactor.pubsub.IPubSubListener;
import com.messagebus.interactor.pubsub.PubsuberManager;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

/**
 * Created by yanghua on 3/18/15.
 */
public class MessagebusPool {

    private static final Log logger = LogFactory.getLog(MessagebusPool.class);

    private String          pubsuberHost;
    private int             pubsuberPort;
    private PubsuberManager pubsuberManager;
    private ConfigManager   configManager;
    private Connection      connection;
    private EventBus        componentEventBus;

    protected InnerPool innerPool;

    public MessagebusPool(String pubsuberHost, int pubsuberPort, GenericObjectPoolConfig poolConfig) {
        this.pubsuberHost = pubsuberHost;
        this.pubsuberPort = pubsuberPort;
        this.init();
        this.innerPool = new InnerPool(poolConfig,
                                       pubsuberHost,
                                       pubsuberPort,
                                       pubsuberManager,
                                       configManager,
                                       connection,
                                       componentEventBus);
    }

    public void registerComponentEventListener(Object listener) {
        this.componentEventBus.register(listener);
    }

    public Messagebus getResource() {
        return this.innerPool.getResource();
    }

    public void returnResource(Messagebus client) {
        this.innerPool.returnResource(client);
    }

    public void destroy() {
        this.innerPool.destroy();

        if (this.connection != null && this.connection.isOpen()) {
            try {
                this.connection.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void init() {
        this.pubsuberManager = new PubsuberManager(this.pubsuberHost, this.pubsuberPort);

        if (!this.pubsuberManager.isPubsuberAlive())
            throw new RuntimeException("can not connect to pubsub server , host : " + this.pubsuberHost
                                           + ", port : " + this.pubsuberPort);

        this.componentEventBus = new AsyncEventBus("componentEventBus", Executors.newSingleThreadExecutor());

        this.configManager = new ConfigManager();
        this.configManager.setPubsuberManager(this.pubsuberManager);
        this.configManager.setComponentEventBus(this.componentEventBus);

        this.registerComponentEventProcessor();

        Map<String, IPubSubListener> channelEventMap = new HashMap<String, IPubSubListener>(5);
        channelEventMap.put(Constants.PUBSUB_NODEVIEW_CHANNEL, configManager.new NodeViewChangedHandler());
        channelEventMap.put(Constants.PUBSUB_CONFIG_CHANNEL, configManager.new ConfigChangedHandler());
        channelEventMap.put(Constants.PUBSUB_SERVER_STATE_CHANNEL, configManager.new ServerStateChangedHandler());
        channelEventMap.put(Constants.PUBSUB_NOTIFY_CHANNEL, configManager.new NotifyHandler());

        this.pubsuberManager.registerWithMultiChannels(channelEventMap);

        ConnectionFactory connectionFactory = null;
        try {
            this.configManager.checkServerState();

            connectionFactory = new ConnectionFactory();
            String host = this.configManager.getConfig("messagebus.client.host");
            connectionFactory.setHost(host);

            if (this.configManager.getPubsuberManager().exists("messagebus.client.port")) {
                int port = Integer.parseInt(this.configManager.getConfig("messagebus.client.port"));
                connectionFactory.setPort(port);
            } else {
                connectionFactory.setPort(connectionFactory.DEFAULT_AMQP_PORT);
            }

            connectionFactory.setAutomaticRecoveryEnabled(true);
            connectionFactory.setTopologyRecoveryEnabled(true);
            connectionFactory.setConnectionTimeout(60000);
            connectionFactory.setRequestedHeartbeat(10);

            this.connection = connectionFactory.newConnection();

        } catch (IOException e) {
            logger.error("init message pool exception with host : " + connectionFactory.getHost()
                             + " and port : " + connectionFactory.getPort(), e);
            throw new RuntimeException("init message pool exception with host : " + connectionFactory.getHost()
                                           + " and port : " + connectionFactory.getPort(), e);
        } catch (TimeoutException e) {
            logger.error("init message pool exception with host : " + connectionFactory.getHost()
                             + " and port : " + connectionFactory.getPort(), e);
            throw new RuntimeException("init message pool exception with host : " + connectionFactory.getHost()
                                           + " and port : " + connectionFactory.getPort(), e);
        } catch (Exception e) {
            logger.error("init message pool exception with host : " + connectionFactory.getHost()
                             + " and port : " + connectionFactory.getPort(), e);
            throw new RuntimeException("init message pool exception with host : " + connectionFactory.getHost()
                                           + " and port : " + connectionFactory.getPort(), e);
        }
    }

    private void registerComponentEventProcessor() {
        this.componentEventBus.register(new ClientDestroyEventProcessor());
        this.componentEventBus.register(new ClientInitedEventProcessor());
    }

}
