package com.messagebus.client;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.messagebus.client.event.component.InnerEvent;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.model.NodeView;
import com.messagebus.common.Constants;
import com.messagebus.common.InnerEventEntity;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.tools.jsonrpc.JsonRpcClient;
import com.rabbitmq.tools.jsonrpc.JsonRpcException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

/**
 * the config manager
 */
public class ConfigManager {

    private static final Log  logger = LogFactory.getLog(ConfigManager.class);
    private static final Gson GSON   = new Gson();

    private Map<String, NodeView> secretNodeViewMap = new ConcurrentHashMap<String, NodeView>();
    private Map<String, String>   configMap         = new ConcurrentHashMap<String, String>();

    private String host;
    private int    port;

    private EventBus componentEventBus;

    public ConfigManager(String host, int port) {
        this.host = host;
        this.port = port;
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

    public NodeView getNodeView(String secret) {
        if (Strings.isNullOrEmpty(secret)) {
            throw new NullPointerException("the secret can not be null or empty");
        }

        if (this.secretNodeViewMap.containsKey(secret)) {   //local cache
            return this.secretNodeViewMap.get(secret);
        } else {                                            //remote data then local cache
//            NodeView nodeViewObj = this.pubsuberManager.get(secret, NodeView.class);
            Object[] params = new Object[]{secret};
            Object responseObj = this.innerRpcRequest("getNodeViewBySecret",
                                                      params);
            if (responseObj != null) {
                NodeView nodeViewObj = GSON.fromJson(responseObj.toString(), NodeView.class);
                this.secretNodeViewMap.put(secret, nodeViewObj);
                return nodeViewObj;
            } else {
                throw new RuntimeException("can not get config info!");
            }
        }
    }

    public class InnerEventProcessor {

        @Subscribe
        public void onInnerEvent(InnerEvent innerEvent) {
            Message msg = innerEvent.getMsg();
            String jsonStr = new String(msg.getContent());
            logger.info("received inner event , content : " + jsonStr);
            InnerEventEntity innerEventObj = GSON.fromJson(jsonStr, InnerEventEntity.class);

            String identifier = innerEventObj.getIdentifier();

            if (identifier.equals(Constants.PUBSUB_NODEVIEW_CHANNEL)) {
                //if local cache not exists just ignore
                if (!Strings.isNullOrEmpty(identifier) && secretNodeViewMap.containsKey(identifier)) {
                    getNodeView(identifier);
                }
            }
        }

    }

    private Object innerRpcRequest(String method, Object[] params) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(this.host);
        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            JsonRpcClient client = new JsonRpcClient(channel,
                                                     Constants.PROXY_EXCHANGE_NAME,
                                                     "routingkey.proxy.message.rpc.configRpcResponse",
                                                     10000);

            return client.call(method, params);
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (JsonRpcException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

}
