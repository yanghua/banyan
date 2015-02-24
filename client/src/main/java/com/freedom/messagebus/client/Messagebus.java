package com.freedom.messagebus.client;

import com.freedom.messagebus.business.exchanger.ExchangerManager;
import com.freedom.messagebus.business.model.Config;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.core.pool.AbstractPool;
import com.freedom.messagebus.client.core.pool.ChannelFactory;
import com.freedom.messagebus.client.core.pool.ChannelPool;
import com.freedom.messagebus.client.core.pool.ChannelPoolConfig;
import com.freedom.messagebus.client.impl.*;
import com.freedom.messagebus.common.Constants;
import com.google.common.base.Strings;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * the main operator of messagebus client
 */
public class Messagebus {

    private static final Log logger = LogFactory.getLog(Messagebus.class);

    private String                appId;
    private ExchangerManager      exchangeManager;
    private ConfigManager         configManager;
    private AbstractPool<Channel> pool;
    private Connection            connection;
    private String                pubsuberHost;
    private int                   pubsuberPort;
    private GenericContext context;

    private AtomicBoolean isOpen         = new AtomicBoolean(false);
    private boolean       useChannelPool = false;


    private Messagebus(String appId) {
        this.appId = appId;
    }

    public static Messagebus createClient(String appId) {
        if (Strings.isNullOrEmpty(appId))
            throw new NullPointerException("the param : appId can not be null or empty");

        return new Messagebus(appId);
    }

    /**
     * this method used to do some init thing before carrying message
     * it will create some expensive big object
     * so do NOT invoke it frequently
     *
     * @throws MessagebusConnectedFailedException
     */
    public synchronized void open() throws MessagebusConnectedFailedException {
        if (this.isOpen())
            return;

        this.exchangeManager = new ExchangerManager(this.getPubsuberHost(), this.getPubsuberPort());

        if (!this.exchangeManager.isZKAlive())
            throw new MessagebusConnectedFailedException("can not connect to zookeeper server.");

        this.configManager = ConfigManager.getInstance();
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

        this.useChannelPool =
            Boolean.valueOf(configManager.getClientConfigMap().get("messagebus.client.useChannelPool").getValue());
        //if use channel pool
        if (this.useChannelPool) {
            this.initChannelPool();
        }

        context = new GenericContext();
        context.setAppId(appId);
        context.setPool(this.pool);
        context.setConfigManager(this.configManager);
        context.setConnection(this.connection);



        boolean success = this.isOpen.compareAndSet(false, true);
        if (!success) {
            logger.error("occurs a non-consistency : the field isOpen should be false but it's actually true");
        }
    }

    /**
     * close the messagebus client and release all used resources
     * pls invoke this method after making sure you will not use the client in
     * current context.
     */
    public synchronized void close() {
        //release all resource
        try {
            if (this.exchangeManager != null)
                this.exchangeManager.removeRegister(this.appId);

            if (this.configManager != null)
                this.configManager.destroy();

            if (this.useChannelPool && pool != null)
                pool.destroy();

            if (this.connection != null && this.connection.isOpen())
                this.connection.close();

            this.isOpen.compareAndSet(true, false);
        } catch (IOException e) {
            logger.error("[close] occurs a IOException : " + e.getMessage());
        }
    }

    public IProducer getProducer() throws MessagebusUnOpenException {
        if (!this.isOpen())
            throw new MessagebusUnOpenException
                ("Illegal State: please call Messagebus#open() first!");

        IProducer producer = GenericProducer.defaultProducer(this.pool);
        producer.setContext(context);
        return producer;
    }

    public AsyncConsumer getAsyncConsumer(IMessageReceiveListener onMessage) {
        if (!this.isOpen())
            throw new MessagebusUnOpenException
                ("Illegal State: please call Messagebus#open() first!");

        AsyncConsumer asyncConsumer = AsyncConsumer.defaultAsyncConsumer(this.pool);
        asyncConsumer.setContext(context);
        asyncConsumer.setOnMessage(onMessage);

        return asyncConsumer;
    }

    public SyncConsumer getSyncConsumer() {
        if (!this.isOpen())
            throw new MessagebusUnOpenException
                ("Illegal State: please call Messagebus#open() first!");

        SyncConsumer syncConsumer = SyncConsumer.defaultSyncConsumer(this.pool);
        syncConsumer.setContext(context);

        return syncConsumer;
    }

    public IRequester getRequester() throws MessagebusUnOpenException {
        if (!this.isOpen())
            throw new MessagebusUnOpenException("Illegal State : please call Messagebus#open() first!");

        IRequester requester = GenericRequester.defaultRequester(pool);
        requester.setContext(context);

        return requester;
    }

    public IResponser getResponser() throws MessagebusUnOpenException {
        if (!this.isOpen())
            throw new MessagebusUnOpenException("Illegal State : please call Messagebus#open() first!");

        IResponser responser = GenericResponser.defaultResponser(this.pool);
        responser.setContext(context);

        return responser;
    }

    public IPublisher getPublisher() throws MessagebusUnOpenException {
        if (!this.isOpen())
            throw new MessagebusUnOpenException("Illegal State : please call Messagebus#open() first!");

        IPublisher publisher = GenericPublisher.defaultPublisher(this.pool);
        publisher.setContext(context);

        return publisher;
    }

    public GenericSubscriber getSubscriber() throws MessagebusUnOpenException {
        if (!this.isOpen())
            throw new MessagebusUnOpenException("Illegal State : please call Messagebus#open() first!");

        GenericSubscriber subscriber = GenericSubscriber.defaultSubscriber(this.pool);
        subscriber.setContext(context);

        return subscriber;
    }

    public IBroadcaster getBroadcaster() throws MessagebusUnOpenException {
        if (!this.isOpen())
            throw new MessagebusUnOpenException("Illegal State : please call Messagebus#open() first!");

        IBroadcaster broadcaster = GenericBroadcaster.defaultBroadcaster(this.pool);
        broadcaster.setContext(context);

        return broadcaster;
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

    private void initConnection() throws MessagebusConnectedFailedException {
        try {
            String host = this.configManager.getClientConfigMap().get("messagebus.client.host").getValue();

            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(host);

            this.connection = connectionFactory.newConnection();
        } catch (IOException e) {
            throw new MessagebusConnectedFailedException(e);
        }
    }

    private void initChannelPool() {
        Map<String, Config> clientConfigs = this.configManager.getClientConfigMap();

        ChannelPoolConfig config = new ChannelPoolConfig();
        config.setMaxTotal(Integer.parseInt(clientConfigs.get("messagebus.client.channel.pool.maxTotal").getValue()));
        config.setMaxIdle(Integer.parseInt(clientConfigs.get("messagebus.client.channel.pool.maxIdle").getValue()));
        config.setMaxWaitMillis(Long.parseLong(clientConfigs.get("messagebus.client.channel.pool.maxWait").getValue()));
        config.setTestOnBorrow(Boolean.valueOf(clientConfigs.get("messagebus.client.channel.pool.testOnBorrow").getValue()));
        config.setTestOnReturn(Boolean.valueOf(clientConfigs.get("messagebus.client.channel.pool.testOnReturn").getValue()));

        pool = new ChannelPool(config, new ChannelFactory(this.connection));
    }

    private boolean doAuth() {
        ConfigManager config = ConfigManager.getInstance();
        return config.getAppIdQueueMap().containsKey(this.appId);
    }

}
