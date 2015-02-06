package com.freedom.messagebus.client;

import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.core.pool.AbstractPool;
import com.freedom.messagebus.interactor.zookeeper.LongLiveZookeeper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * generic context. contains both general object
 * and those object that it owns resource that must be destroyed
 */
public class GenericContext {
    
    private LongLiveZookeeper     zooKeeper;
    private ConfigManager         configManager;
    private AbstractPool<Channel> pool;
    private Connection            connection;
    private String                appId;

    public GenericContext() {
    }


    public LongLiveZookeeper getZooKeeper() {
        return zooKeeper;
    }

    public void setZooKeeper(LongLiveZookeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }


    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public AbstractPool<Channel> getPool() {
        return pool;
    }

    public void setPool(AbstractPool<Channel> pool) {
        this.pool = pool;
    }


    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }


    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Override
    public String toString() {
        return "GeneralContext{}";
    }
}
