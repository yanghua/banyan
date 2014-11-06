package com.freedom.messagebus.client;

import com.freedom.messagebus.client.core.authorize.Authorizer;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.core.pool.AbstractPool;
import com.freedom.messagebus.client.core.pool.ChannelFactory;
import com.freedom.messagebus.client.core.pool.ChannelPool;
import com.freedom.messagebus.client.core.pool.ChannelPoolConfig;
import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.common.message.MessageFactory;
import com.freedom.messagebus.common.message.MessageType;
import com.freedom.messagebus.common.model.Config;
import com.freedom.messagebus.interactor.zookeeper.IConfigChangedListener;
import com.freedom.messagebus.interactor.zookeeper.LongLiveZookeeper;
import com.freedom.messagebus.interactor.zookeeper.ZKEventType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * the main operator of messagebus client
 */
public class Messagebus {

    private static final Log logger = LogFactory.getLog(Messagebus.class);

    private static volatile Messagebus instance = null;

    @NotNull
    private String       appId;
    @NotNull
    private IProducer    producer;
    @NotNull
    private IConsumer    consumer;
    @NotNull
    private IRequester   requester;
    @NotNull
    private IResponser   responser;
    @NotNull
    private IPublisher   publisher;
    @NotNull
    private ISubscriber  subscriber;
    @NotNull
    private IBroadcaster broadcaster;

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

    private Messagebus(String appId) {
        this.appId = appId;
    }

    public static Messagebus getInstance(String appId) {
        if (instance == null) {
            synchronized (Messagebus.class) {
                if (instance == null) {
                    instance = new Messagebus(appId);
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
        this.zookeeper = new LongLiveZookeeper(this.getZkHost(), this.getZkPort());

        if (!this.zookeeper.isAlive())
            throw new MessagebusConnectedFailedException("can not connect to zookeeper server.");
        else {
            fetchNewZookeeperData();
        }

        this.configManager = ConfigManager.getInstance();
        this.zookeeper.watchPaths(
            new String[]{
                CONSTS.ZOOKEEPER_ROOT_PATH_FOR_ROUTER,
                CONSTS.ZOOKEEPER_ROOT_PATH_FOR_CONFIG,
                CONSTS.ZOOKEEPER_ROOT_PATH_FOR_EVENT
            },
            new IConfigChangedListener() {
                @Override
                public void onChanged(String path,
                                      byte[] newData,
                                      ZKEventType eventType) {
                    logger.debug("path : " + path + " has changed!");

                    try {
                        if (path.equals(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_ROUTER)) {
                            refreshLocalCachedFile(path, newData);
                            ConfigManager.getInstance().parseRouterInfo();
                        } else if (path.equals(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_EVENT)) {
                            String newState = new String(newData);
                            logger.info("messagebus server event : " + newState);
                            ConfigManager.getInstance().setServerState(newState);
                        }
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
        context.setAppId(appId);
        context.setPool(this.pool);
        context.setConfigManager(this.configManager);
        context.setZooKeeper(this.zookeeper);
        context.setConnection(this.connection);

        producer = new GenericProducer(context);
        consumer = new GenericConsumer(context);
        requester = new GenericRequester(context);
        responser = new GenericResponser(context);
        publisher = new GenericPublisher(context);
        subscriber = new GenericSubscriber(context);
        broadcaster = new GenericBroadcaster(context);

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
            if (this.configManager != null)
                this.configManager.destroy();

            if (this.useChannelPool && pool != null)
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

    @NotNull
    public IPublisher getPublisher() throws MessagebusUnOpenException {
        if (!this.isOpen())
            throw new MessagebusUnOpenException("Illegal State : please call Messagebus#open() first!");

        return publisher;
    }

    @NotNull
    public ISubscriber getSubscriber() throws MessagebusUnOpenException {
        if (!this.isOpen())
            throw new MessagebusUnOpenException("Illegal State : please call Messagebus#open() first!");

        return subscriber;
    }

    @NotNull
    public IBroadcaster getBroadcaster() throws MessagebusUnOpenException {
        if (!this.isOpen())
            throw new MessagebusUnOpenException("Illegal State : please call Messagebus#open() first!");

        return broadcaster;
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

    private synchronized void fetchNewZookeeperData() {
        //get new config info
        byte[] routerData = this.zookeeper.getConfig(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_ROUTER);
        byte[] configData = this.zookeeper.getConfig(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_CONFIG);
        byte[] eventData = this.zookeeper.getConfig(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_EVENT);
        //refresh local
        this.refreshLocalCachedFile(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_ROUTER, routerData);
        this.refreshLocalCachedFile(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_CONFIG, configData);
        ConfigManager.getInstance().setServerState(new String(eventData));
    }

    private synchronized void refreshLocalCachedFile(String path, byte[] newData) {
        String filePath;
        if (path.equals(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_ROUTER)) {
            filePath = CONSTS.EXPORTED_NODE_FILE_PATH;
        } else if (path.equals(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_CONFIG)) {
            filePath = CONSTS.EXPORTED_CONFIG_FILE_PATH;
        } else {
            return;
        }

        Path routerFilePath = FileSystems.getDefault().getPath(filePath);
        FileOutputStream fos = null;
        try {
            if (!Files.exists(routerFilePath)) { //override
                Files.createFile(routerFilePath);
            }

            fos = new FileOutputStream(filePath);
            fos.write(newData);
        } catch (IOException e) {
            logger.error("[refreshLocalCachedRouterFile] occurs a IOException : " + e.getMessage());
        } finally {
            try {
                fos.flush();
                fos.close();
            } catch (IOException e) {
                logger.error("[refreshLocalCachedRouterFile] finally block occurs a IOException : " + e.getMessage());
            }
        }
    }
}
