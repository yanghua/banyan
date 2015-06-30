package com.messagebus.client;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.messagebus.client.event.component.NotifyEvent;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.client.model.Node;
import com.messagebus.client.model.NodeView;
import com.messagebus.common.Constants;
import com.messagebus.interactor.pubsub.IPubSubListener;
import com.messagebus.interactor.pubsub.PubsuberManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * the config manager
 */
public class ConfigManager {

    private static final Log logger = LogFactory.getLog(ConfigManager.class);

    private volatile String                serverState       = Constants.MESSAGEBUS_SERVER_EVENT_STOPPED;
    private          Map<String, NodeView> secretNodeViewMap = new ConcurrentHashMap<String, NodeView>();
    private          Map<String, String>   configMap         = new ConcurrentHashMap<String, String>();

    private PubsuberManager pubsuberManager;
    private EventBus        componentEventBus;

    public ConfigManager() {
    }

    public PubsuberManager getPubsuberManager() {
        return pubsuberManager;
    }

    public void setPubsuberManager(PubsuberManager pubsuberManager) {
        this.pubsuberManager = pubsuberManager;
    }

    public EventBus getComponentEventBus() {
        return componentEventBus;
    }

    public void setComponentEventBus(EventBus componentEventBus) {
        this.componentEventBus = componentEventBus;
    }

    public Map<String, NodeView> getSecretNodeViewMap() {
        return secretNodeViewMap;
    }

    public Map<String, String> getConfigMap() {
        return configMap;
    }

    public String getServerState() {
        return serverState;
    }

    public synchronized void setServerState(String serverState) {
        this.serverState = serverState;
    }

    public void checkServerState() {
        String tmp = this.getPubsuberManager().get(Constants.PUBSUB_SERVER_STATE_CHANNEL, String.class);
        logger.debug("current server state is : " + tmp);
        if (tmp != null) {
            this.setServerState(tmp);
        }
    }

    public String getConfig(String key) {
        if (this.configMap.containsKey(key)) {
            return this.configMap.get(key);
        } else {
            String configValue = this.pubsuberManager.get(key, String.class);
            this.configMap.put(key, configValue);
            return configValue;
        }
    }

    public NodeView getNodeView(String secret) {
        if (Strings.isNullOrEmpty(secret)) {
            throw new NullPointerException("the secret can not be null or empty");
        }

        if (this.secretNodeViewMap.containsKey(secret)) {   //local cache
            return this.secretNodeViewMap.get(secret);
        } else {                                            //remote data then local cache
            NodeView nodeViewObj = this.pubsuberManager.get(secret, NodeView.class);
            this.secretNodeViewMap.put(secret, nodeViewObj);
            return nodeViewObj;
        }
    }

    public class ConfigChangedHandler implements IPubSubListener {

        @Override
        public void onChange(String channel, byte[] data, Map<String, Object> params) {
            String key = new String(data);

            if (getConfigMap().containsKey(key)) {
                getConfigMap().remove(key);
                getConfig(key);
            }
        }
    }

    public class NodeViewChangedHandler implements IPubSubListener {

        @Override
        public void onChange(String channel, byte[] data, Map<String, Object> params) {
            if (getSecretNodeViewMap().containsKey(channel)) {
                getSecretNodeViewMap().remove(channel);
                getNodeView(channel);
            }
        }

    }

    public class ServerStateChangedHandler implements IPubSubListener {

        @Override
        public void onChange(String channel, byte[] data, Map<String, Object> params) {
            String currentState = new String(data);
            setServerState(currentState);
        }

    }

    public class NotifyHandler implements IPubSubListener {

        @Override
        public void onChange(String channel, byte[] data, Map<String, Object> params) {
            NotifyEvent notifyEvent = new NotifyEvent();
            Message broadcastMsg = pubsuberManager.deserialize(data, Message.class);
            if (broadcastMsg != null && broadcastMsg.getMessageType().equals(MessageType.BroadcastMessage)) {
                notifyEvent.setMsg(broadcastMsg);
                getComponentEventBus().post(notifyEvent);
            }
        }
    }

}
