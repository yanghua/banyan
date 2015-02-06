package com.freedom.messagebus.client;

import com.freedom.messagebus.business.exchanger.ExchangerManager;
import com.freedom.messagebus.business.model.Config;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.core.pool.AbstractPool;
import com.freedom.messagebus.client.core.pool.ChannelFactory;
import com.freedom.messagebus.client.core.pool.ChannelPool;
import com.freedom.messagebus.client.core.pool.ChannelPoolConfig;
import com.freedom.messagebus.common.CONSTS;
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


    private String appId;

    private IProducer producer;

    private IConsumer consumer;

    private IRequester requester;

    private IResponser responser;

    private IPublisher publisher;

    private ISubscriber subscriber;

    private IBroadcaster broadcaster;

    private ExchangerManager zkExchangeManager;

    private ConfigManager         configManager;
    private AbstractPool<Channel> pool;

    private Connection connection;

    private AtomicBoolean isOpen         = new AtomicBoolean(false);
    private boolean       useChannelPool = false;


    private String zkHost;
    private int    zkPort;

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

        this.zkExchangeManager = ExchangerManager.defaultExchangerManager(this.getZkHost(), this.getZkPort());

        if (!this.zkExchangeManager.isZKAlive())
            throw new MessagebusConnectedFailedException("can not connect to zookeeper server.");

        this.configManager = ConfigManager.getInstance();
        this.configManager.setZKExchangeManager(this.zkExchangeManager);
        this.zkExchangeManager.registerWithMultiPaths(new String[]{
            CONSTS.ZOOKEEPER_ROOT_PATH_FOR_ROUTER,
            CONSTS.ZOOKEEPER_ROOT_PATH_FOR_CONFIG,
            CONSTS.ZOOKEEPER_ROOT_PATH_FOR_EVENT,
            CONSTS.ZOOKEEPER_ROOT_PATH_FOR_AUTH,
            CONSTS.ZOOKEEPER_PATH_FOR_AUTH_SEND_PERMISSION,
            CONSTS.ZOOKEEPER_PATH_FOR_AUTH_RECEIVE_PERMISSION
        }, this.configManager);

        try {
            this.configManager.parseZKData();
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

        GenericContext context = new GenericContext();
        context.setAppId(appId);
        context.setPool(this.pool);
        context.setConfigManager(this.configManager);
        context.setConnection(this.connection);

        ServiceLoader<IProducer> producerLoader = ServiceLoader.load(IProducer.class);
        producer = producerLoader.iterator().next();
        producer.setContext(context);

        ServiceLoader<IConsumer> consumerLoader = ServiceLoader.load(IConsumer.class);
        consumer = consumerLoader.iterator().next();
        consumer.setContext(context);

        ServiceLoader<IRequester> requestLoader = ServiceLoader.load(IRequester.class);
        requester = requestLoader.iterator().next();
        requester.setContext(context);

        ServiceLoader<IResponser> responseLoader = ServiceLoader.load(IResponser.class);
        responser = responseLoader.iterator().next();
        responser.setContext(context);

        ServiceLoader<IPublisher> publisherLoader = ServiceLoader.load(IPublisher.class);
        publisher = publisherLoader.iterator().next();
        publisher.setContext(context);

        ServiceLoader<ISubscriber> subscriberLoader = ServiceLoader.load(ISubscriber.class);
        subscriber = subscriberLoader.iterator().next();
        subscriber.setContext(context);

        ServiceLoader<IBroadcaster> broadcasterLoader = ServiceLoader.load(IBroadcaster.class);
        broadcaster = broadcasterLoader.iterator().next();
        broadcaster.setContext(context);

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
            if (this.zkExchangeManager != null)
                this.zkExchangeManager.removeRegister(this.configManager);

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


    public synchronized IProducer getProducer() throws MessagebusUnOpenException {
        if (!this.isOpen())
            throw new MessagebusUnOpenException
                ("Illegal State: please call Messagebus#open() first!");

        return producer;
    }


    public synchronized IConsumer getConsumer() throws MessagebusUnOpenException {
        if (!this.isOpen())
            throw new MessagebusUnOpenException
                ("Illegal State: please call Messagebus#open() first!");

        return consumer;
    }


    public synchronized IRequester getRequester() throws MessagebusUnOpenException {
        if (!this.isOpen())
            throw new MessagebusUnOpenException("Illegal State : please call Messagebus#open() first!");

        return requester;
    }


    public synchronized IResponser getResponser() throws MessagebusUnOpenException {
        if (!this.isOpen())
            throw new MessagebusUnOpenException("Illegal State : please call Messagebus#open() first!");

        return responser;
    }


    public synchronized IPublisher getPublisher() throws MessagebusUnOpenException {
        if (!this.isOpen())
            throw new MessagebusUnOpenException("Illegal State : please call Messagebus#open() first!");

        return publisher;
    }


    public synchronized ISubscriber getSubscriber() throws MessagebusUnOpenException {
        if (!this.isOpen())
            throw new MessagebusUnOpenException("Illegal State : please call Messagebus#open() first!");

        return subscriber;
    }


    public synchronized IBroadcaster getBroadcaster() throws MessagebusUnOpenException {
        if (!this.isOpen())
            throw new MessagebusUnOpenException("Illegal State : please call Messagebus#open() first!");

        return broadcaster;
    }

    public boolean isOpen() {
        return this.isOpen.get();
    }


    public String getZkHost() {
        if (this.zkHost == null || this.zkHost.isEmpty())
            this.zkHost = "localhost";

        return zkHost;
    }

    public void setZkHost(String zkHost) {
        this.zkHost = zkHost;
    }

    public int getZkPort() {
        if (this.zkPort == 0)
            this.zkPort = 2181;

        return zkPort;
    }

    public void setZkPort(int zkPort) {
        this.zkPort = zkPort;
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
