package com.freedom.messagebus.client;

import com.freedom.messagebus.client.core.config.ConfigManager;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

/**
 * generic context. contains both general object
 * and those object that it owns resource that must be destroyed
 */
public class GenericContext {

    private ConfigManager configManager;
    private Channel       channel;
    private Connection    connection;
    private String        appId;

    public GenericContext() {
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
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
