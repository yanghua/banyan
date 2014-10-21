package com.freedom.messagebus.client;

import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.core.config.LongLiveZookeeper;
import com.freedom.messagebus.client.core.pool.AbstractPool;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.jetbrains.annotations.NotNull;

/**
 * generic context. contains both general object
 * and those object that it owns resource that must be destroyed
 */
public class GenericContext {

    @NotNull
    private LongLiveZookeeper     zooKeeper;
    @NotNull
    private ConfigManager         configManager;
    private AbstractPool<Channel> pool;
    @NotNull
    private Connection            connection;
    @NotNull
    private String                appId;

    public GenericContext() {
    }

    @NotNull
    public LongLiveZookeeper getZooKeeper() {
        return zooKeeper;
    }

    public void setZooKeeper(@NotNull LongLiveZookeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    @NotNull
    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void setConfigManager(@NotNull ConfigManager configManager) {
        this.configManager = configManager;
    }

    public AbstractPool<Channel> getPool() {
        return pool;
    }

    public void setPool(AbstractPool<Channel> pool) {
        this.pool = pool;
    }

    @NotNull
    public Connection getConnection() {
        return connection;
    }

    public void setConnection(@NotNull Connection connection) {
        this.connection = connection;
    }

    @NotNull
    public String getAppId() {
        return appId;
    }

    public void setAppId(@NotNull String appId) {
        this.appId = appId;
    }

    @Override
    public String toString() {
        return "GeneralContext{}";
    }
}
