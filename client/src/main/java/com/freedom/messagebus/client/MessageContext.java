package com.freedom.messagebus.client;

import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.core.pool.AbstractPool;
import com.freedom.messagebus.client.handler.consumer.OriginalReceiver;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.client.model.MessageFormat;
import com.freedom.messagebus.client.model.MsgBytes;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.ZooKeeper;
import org.jetbrains.annotations.NotNull;
import com.freedom.messagebus.common.message.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * the message context, mostly used in handler chain
 */
public class MessageContext {

    private static final Log logger = LogFactory.getLog(MessageContext.class);

    @NotNull
    public Connection connection;

    @NotNull
    private String  host;
    private boolean isAuthorized;
    private boolean enableTransaction;
    @NotNull
    private String  appKey;

    /**
     * for produce
     */
    @NotNull
    private com.freedom.messagebus.common.message.Message[] messages;

    /**
     * for consume
     */
    @NotNull
    private Message consumedMsg;
    @NotNull
    private byte[]  consumedMsgBytes;

    @NotNull
    private MessageCarryType carryType;     //produce or consume

    /**
     * if carry type is produce then it means : routingKey
     * else if carry type is consume then it means queueName.
     * <p/>
     * if this field's value is a empty string that means
     * it will be matched with a generic rule like : #
     * Note:
     * when carry type is consume, this field can not be null or empty.
     * It will be validated by a handle named : ParamValidator
     */
    @NotNull
    private String                  ruleValue;
    @NotNull
    private Channel                 channel;
    @NotNull
    private OriginalReceiver        receiver;
    @NotNull
    private IChannelDestroyer       destroyer;
    @NotNull
    private IMessageReceiveListener listener;
    @NotNull
    private Map<String, Object> otherParams = new HashMap<String, Object>();

    /**
     * for resource recycle
     */
    @NotNull
    private ZooKeeper             zooKeeper;
    @NotNull
    private ConfigManager         configManager;
    private AbstractPool<Channel> pool;

    public MessageContext() {
    }

    @NotNull
    public String getHost() {
        if (this.host == null) {
            this.host = this.configManager.getConfigProperty().getProperty("messagebus.client.host");
            return this.host;
        }
        return this.host;
    }

    @NotNull
    public Connection getConnection() {
        return connection;
    }

    public void setConnection(@NotNull Connection connection) {
        this.connection = connection;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
    }

    public boolean isEnableTransaction() {
        return enableTransaction;
    }

    public void setEnableTransaction(boolean enableTransaction) {
        this.enableTransaction = enableTransaction;
    }

    @NotNull
    public Message[] getMessages() {
        return messages;
    }

    public void setMessages(@NotNull Message[] messages) {
        this.messages = messages;
    }

    @NotNull
    public Channel getChannel() {
        return channel;
    }

    public void setChannel(@NotNull Channel channel) {
        this.channel = channel;
    }

    @NotNull
    public OriginalReceiver getReceiver() {
        return receiver;
    }

    public void setReceiver(@NotNull OriginalReceiver receiver) {
        this.receiver = receiver;
    }

    @NotNull
    public MessageCarryType getCarryType() {
        return carryType;
    }

    public void setCarryType(@NotNull MessageCarryType carryType) {
        this.carryType = carryType;
    }

    @NotNull
    public Map<String, Object> getOtherParams() {
        return otherParams;
    }

    @NotNull
    public String getRuleValue() {
        return ruleValue;
    }

    public void setRuleValue(@NotNull String ruleValue) {
        this.ruleValue = ruleValue;
    }

    @NotNull
    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(@NotNull String appKey) {
        this.appKey = appKey;
    }

    @NotNull
    public IChannelDestroyer getDestroyer() {
        return destroyer;
    }

    public void setDestroyer(@NotNull IChannelDestroyer destroyer) {
        this.destroyer = destroyer;
    }

    @NotNull
    public Message getConsumedMsg() {
        return consumedMsg;
    }

    public void setConsumedMsg(@NotNull Message consumedMsg) {
        this.consumedMsg = consumedMsg;
    }

    @NotNull
    public byte[] getConsumedMsgBytes() {
        return consumedMsgBytes;
    }

    public void setConsumedMsgBytes(@NotNull byte[] consumedMsgBytes) {
        this.consumedMsgBytes = consumedMsgBytes;
    }

    @NotNull
    public IMessageReceiveListener getListener() {
        return listener;
    }

    public void setListener(@NotNull IMessageReceiveListener listener) {
        this.listener = listener;
    }

    @NotNull
    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }

    public void setZooKeeper(@NotNull ZooKeeper zooKeeper) {
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

    @Override
    public String toString() {
        return "MessageContext";
    }
}
