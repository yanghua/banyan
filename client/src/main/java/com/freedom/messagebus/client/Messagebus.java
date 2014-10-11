package com.freedom.messagebus.client;

import com.freedom.messagebus.client.core.authorize.Authorizer;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.core.config.IConfigChangedListener;
import com.freedom.messagebus.client.core.config.LongLiveZookeeper;
import com.freedom.messagebus.client.core.pool.AbstractPool;
import com.freedom.messagebus.client.core.pool.ChannelFactory;
import com.freedom.messagebus.client.core.pool.ChannelPool;
import com.freedom.messagebus.client.core.pool.ChannelPoolConfig;
import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.common.message.MessageFactory;
import com.freedom.messagebus.common.message.MessageType;
import com.freedom.messagebus.common.model.Config;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.Watcher;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * the main operator of messagebus client
 */
public class Messagebus {

    private static final Log logger = LogFactory.getLog(Messagebus.class);

    private static volatile Messagebus instance = null;

    @NotNull
    private String     appKey;
    @NotNull
    private IProducer  producer;
    @NotNull
    private IConsumer  consumer;
    @NotNull
    private IRequester requester;
    @NotNull
    private IResponser responser;

    @NotNull
    private LongLiveZookeeper     zookeeper;
    @NotNull
    private ConfigManager         configManager;
    private AbstractPool<Channel> pool;
    @NotNull
    private Connection            connection;

    private AtomicBoolean isOpen         = new AtomicBoolean(false);
    private boolean       useChannelPool = false;


    @NotNull
    private String zkHost;
    private int    zkPort;

    private Messagebus(String appKey) {
        this.appKey = appKey;
    }

    public static Messagebus getInstance(String appKey) {
        if (instance == null) {
            synchronized (Messagebus.class) {
                if (instance == null) {
                    instance = new Messagebus(appKey);
                }
            }
        }

        return instance;
    }

    /**
     * this method used to do some init thing before carrying message
     * it will create some expensive big object
     * so do NOT invoke it frequently
     *
     * @throws MessagebusConnectedFailedException
     */
    public synchronized void open() throws MessagebusConnectedFailedException {
        //load class
        this.zookeeper = LongLiveZookeeper.getZKInstance(this.getZkHost(), this.getZkPort());

        if (!this.zookeeper.isAlive())
            throw new MessagebusConnectedFailedException("can not connect to zookeeper server.");

        this.configManager = ConfigManager.getInstance();
        this.zookeeper.watchPaths(new String[]{CONSTS.ZOOKEEPER_ROOT_PATH_FOR_ROUTER},
                                  new IConfigChangedListener() {
                                      @Override
                                      public void onChanged(String path,
                                                            byte[] newData,
                                                            Watcher.Event.EventType eventType) {
                                          logger.info("path : " + path + " has changed!");

                                          try {
                                              if (path.equals(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_ROUTER))
                                                  ConfigManager.getInstance().parseRouterInfo();
                                          } catch (Exception e) {
                                              logger.error("[onChanged] occurs a Exception : " + e.getMessage());
                                          }
                                      }
                                  }
                                 );

        this.initConnection();

        this.doAuth();

        this.useChannelPool =
            Boolean.valueOf(configManager.getClientConfigMap().get("messagebus.client.useChannelPool").getValue());
        //if use channel pool
        if (this.useChannelPool) {
            this.initChannelPool();
        }

        GenericContext context = new GenericContext();
        context.setAppKey(appKey);
        context.setPool(this.pool);
        context.setConfigManager(this.configManager);
        context.setZooKeeper(this.zookeeper);
        context.setConnection(this.connection);

        producer = new GenericProducer(context);
        consumer = new GenericConsumer(context);
        requester = new GenericRequester(context);
        responser = new GenericResponser(context);

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
            this.configManager.destroy();

            if (this.useChannelPool)
                pool.destroy();

            if (this.connection.isOpen())
                this.connection.close();

            this.zookeeper.close();

            boolean success = this.isOpen.compareAndSet(true, false);
            if (!success) {
                logger.error("occurs a non-consistency : the field isOpen should be true but it's actually false");
            }
        } catch (IOException e) {
            logger.error("[close] occurs a IOException : " + e.getMessage());
        }
    }

    @NotNull
    public IProducer getProducer() throws MessagebusUnOpenException {
        if (!this.isOpen())
            throw new MessagebusUnOpenException
                ("Illegal State: please call Messagebus#open() first!");

        return producer;
    }

    @NotNull
    public IConsumer getConsumer() throws MessagebusUnOpenException {
        if (!this.isOpen())
            throw new MessagebusUnOpenException
                ("Illegal State: please call Messagebus#open() first!");

        return consumer;
    }

    @NotNull
    public IRequester getRequester() throws MessagebusUnOpenException {
        if (!this.isOpen())
            throw new MessagebusUnOpenException("Illegal State : please call Messagebus#open() first!");

        return requester;
    }

    @NotNull
    public IResponser getResponser() throws MessagebusUnOpenException {
        if (!this.isOpen())
            throw new MessagebusUnOpenException("Illegal State : please call Messagebus#open() first!");

        return responser;
    }

    public boolean isOpen() {
        return this.isOpen.get();
    }

    @NotNull
    public String getZkHost() {
        if (this.zkHost == null || this.zkHost.isEmpty())
            this.zkHost = "localhost";

        return zkHost;
    }

    public void setZkHost(@NotNull String zkHost) {
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
        config.setMaxTotal(Integer.valueOf(clientConfigs.get("messagebus.client.channel.pool.maxTotal").getValue()));
        config.setMaxIdle(Integer.valueOf(clientConfigs.get("messagebus.client.channel.pool.maxIdle").getValue()));
        config.setMaxWaitMillis(Long.valueOf(clientConfigs.get("messagebus.client.channel.pool.maxWait").getValue()));
        config.setTestOnBorrow(Boolean.valueOf(clientConfigs.get("messagebus.client.channel.pool.testOnBorrow").getValue()));
        config.setTestOnReturn(Boolean.valueOf(clientConfigs.get("messagebus.client.channel.pool.testOnReturn").getValue()));

        pool = new ChannelPool(config, new ChannelFactory(this.connection));
    }

    private boolean doAuth() {
        //auth request
        Message authReqMsg = MessageFactory.createMessage(MessageType.AuthreqMessage);
        Authorizer authorizer = Authorizer.getInstance();
//        authorizer.syncRequestAuthorize();

        //TODO
        return false;
    }
}
