package com.messagebus.client;

import com.google.common.eventbus.EventBus;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.model.MessageCarryType;
import com.messagebus.client.model.Node;
import com.messagebus.interactor.pubsub.PubsuberManager;
import com.rabbitmq.client.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * the message context, mostly used in handler chain
 */
public class MessageContext {

    private static final Log logger = LogFactory.getLog(MessageContext.class);

    private String                  host;
    private String                  secret;
    private String                  token;
    private boolean                 enableTransaction;
    private Message[]               messages;
    private String                  consumerTag;
    private MessageCarryType        carryType;
    private Node                    sourceNode;
    private Node                    targetNode;
    private Channel                 channel;
    private IMessageReceiveListener receiveListener;
    private long                    timeout;
    private TimeUnit                timeoutUnit;
    private boolean                 hasTimeout;
    private int                     consumeMsgNum;
    private List<Message>           consumeMsgs;
    private String                  tempQueueName;
    private ConfigManager           configManager;
    private PubsuberManager         pubsuberManager;
    private IMessageReceiveListener noticeListener;
    private IRequestListener        requestListener;
    private EventBus                carryEventBus;

    private Map<String, Object> otherParams = new HashMap<String, Object>();
    private boolean             isSync      = false;

    public MessageContext() {
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isEnableTransaction() {
        return enableTransaction;
    }

    public void setEnableTransaction(boolean enableTransaction) {
        this.enableTransaction = enableTransaction;
    }

    public Message[] getMessages() {
        return messages;
    }

    public void setMessages(Message[] messages) {
        this.messages = messages;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public MessageCarryType getCarryType() {
        return carryType;
    }

    public void setCarryType(MessageCarryType carryType) {
        this.carryType = carryType;
    }

    public Node getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(Node targetNode) {
        this.targetNode = targetNode;
    }

    public Node getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(Node sourceNode) {
        this.sourceNode = sourceNode;
    }

    public Map<String, Object> getOtherParams() {
        return otherParams;
    }

    public IMessageReceiveListener getReceiveListener() {
        return receiveListener;
    }

    public void setReceiveListener(IMessageReceiveListener receiveListener) {
        this.receiveListener = receiveListener;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public TimeUnit getTimeoutUnit() {
        return timeoutUnit;
    }

    public void setTimeoutUnit(TimeUnit timeoutUnit) {
        this.timeoutUnit = timeoutUnit;
    }

    public boolean isTimeout() {
        return hasTimeout;
    }

    public void setIsTimeout(boolean hasTimeout) {
        this.hasTimeout = hasTimeout;
    }

    public String getTempQueueName() {
        return tempQueueName;
    }

    public void setTempQueueName(String tempQueueName) {
        this.tempQueueName = tempQueueName;
    }

    public int getConsumeMsgNum() {
        return consumeMsgNum;
    }

    public void setConsumeMsgNum(int consumeMsgNum) {
        this.consumeMsgNum = consumeMsgNum;
    }

    public List<Message> getConsumeMsgs() {
        return consumeMsgs;
    }

    public void setConsumeMsgs(List<Message> consumeMsgs) {
        this.consumeMsgs = consumeMsgs;
    }

    public boolean isSync() {
        return isSync;
    }

    public void setSync(boolean isSync) {
        this.isSync = isSync;
    }

    public String getConsumerTag() {
        return consumerTag;
    }

    public void setConsumerTag(String consumerTag) {
        this.consumerTag = consumerTag;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    public PubsuberManager getPubsuberManager() {
        return pubsuberManager;
    }

    public void setPubsuberManager(PubsuberManager pubsuberManager) {
        this.pubsuberManager = pubsuberManager;
    }

    public IRequestListener getRequestListener() {
        return requestListener;
    }

    public void setRequestListener(IRequestListener requestListener) {
        this.requestListener = requestListener;
    }

    public EventBus getCarryEventBus() {
        return carryEventBus;
    }

    public void setCarryEventBus(EventBus carryEventBus) {
        this.carryEventBus = carryEventBus;
    }

    @Override
    public String toString() {
        return "MessageContext";
    }
}
