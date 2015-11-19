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
        } else {                                            //remote data then save to local cache
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
//            InnerEventEntity innerEventObj = GSON.fromJson(jsonStr, InnerEventEntity.class);

            refreshNodeView();
        }
    }

    private void refreshNodeView() {
        for (String secret : this.secretNodeViewMap.keySet()) {
            Object[] params = new Object[]{secret};
            Object responseObj = this.innerRpcRequest("getNodeViewBySecret",
                                                      params);
            if (responseObj != null) {
                NodeView nodeViewObj = GSON.fromJson(responseObj.toString(), NodeView.class);
                this.secretNodeViewMap.put(secret, nodeViewObj);
            } else {
                throw new RuntimeException("can not get config info!");
            }
        }
    }

    private Object innerRpcRequest(String method, Object[] params) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(this.host);
        Connection connection = null;
        Channel channel = null;
        JsonRpcClient client = null;
        try {
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
            client = new JsonRpcClient(channel,
                                                     Constants.PROXY_EXCHANGE_NAME,
                                                     Constants.DEFAULT_CONFIG_RPC_RESPONSE_ROUTING_KEY,
                                                     30000);

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
        } finally {
            try {
                if (client != null) {
                    client.close();
                }

                if (channel != null && channel.isOpen()) {
                    channel.close();
                }
                if (connection != null && connection.isOpen()) {
                    connection.close();
                }
            } catch (IOException e) {
                logger.error(e);
            } catch (TimeoutException e) {
                logger.error(e);
            }
        }
    }

}
