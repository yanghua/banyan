package com.freedom.messagebus.client;

import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.handler.common.AsyncEventLoop;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.rabbitmq.client.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * the message context, mostly used in handler chain
 */
public class MessageContext {

    private static final Log logger = LogFactory.getLog(MessageContext.class);

    private String                  host;
    private boolean                 isAuthorized;
    private boolean                 enableTransaction;
    private String                  appId;
    private Message[]               messages;
    private Message                 consumedMsg;
    private String                  consumerTag;
    private MessageCarryType        carryType;
    private Node                    sourceNode;
    private Node                    targetNode;
    private Channel                 channel;
    private AsyncEventLoop          asyncEventLoop;
    private IMessageReceiveListener listener;
    private long                    timeout;
    private boolean                 hasTimeout;
    private int                     consumeMsgNum;
    private List<Message>           consumeMsgs;
    private String                  tempQueueName;
    private List<String>            subQueueNames;
    private ConfigManager           configManager;

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

    public AsyncEventLoop getAsyncEventLoop() {
        return asyncEventLoop;
    }

    public void setAsyncEventLoop(AsyncEventLoop asyncEventLoop) {
        this.asyncEventLoop = asyncEventLoop;
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

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Message getConsumedMsg() {
        return consumedMsg;
    }

    public void setConsumedMsg(Message consumedMsg) {
        this.consumedMsg = consumedMsg;
    }

    public IMessageReceiveListener getListener() {
        return listener;
    }

    public void setListener(IMessageReceiveListener listener) {
        this.listener = listener;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
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

    public List<String> getSubQueueNames() {
        return subQueueNames;
    }

    public void setSubQueueNames(List<String> subQueueNames) {
        this.subQueueNames = subQueueNames;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }

    @Override
    public String toString() {
        return "MessageContext";
    }
}
