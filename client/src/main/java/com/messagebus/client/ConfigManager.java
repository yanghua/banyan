package com.messagebus.client;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.Gson;
import com.messagebus.client.event.component.InnerEvent;
import com.messagebus.client.message.model.Message;
import com.messagebus.common.Constants;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.tools.jsonrpc.JsonRpcClient;
import com.rabbitmq.tools.jsonrpc.JsonRpcException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * the config manager
 */
public class ConfigManager {

    private static final Log  logger = LogFactory.getLog(ConfigManager.class);
    private static final Gson GSON   = new Gson();

    private Map<String, Source> secretSourceMap = new HashMap<String, Source>();
    private Map<String, Source> nameSourceMap   = new HashMap<String, Source>();
    private Map<String, Sink>   secretSinkMap   = new HashMap<String, Sink>();
    private Map<String, Sink>   nameSinkMap     = new HashMap<String, Sink>();
    private Map<String, Stream> streamMap       = new HashMap<String, Stream>();

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

    public Source getSourceBySecret(String secret) {
        if (Strings.isNullOrEmpty(secret)) {
            throw new NullPointerException("the secret can not be null or empty");
        }

        if (this.secretSourceMap.containsKey(secret)) {     //local cache
            return this.secretSourceMap.get(secret);
        } else {                                            //remote data then save to local cache
            Object[] params = new Object[]{secret};
            Object responseObj = this.innerRpcRequest("getSourceBySecret",
                                                      params);
            if (responseObj != null) {
                Source source = GSON.fromJson(responseObj.toString(), Source.class);
                this.secretSourceMap.put(secret, source);
                return source;
            } else {
                throw new RuntimeException("can not get source info!");
            }
        }
    }

    public Source getSourceByName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            throw new NullPointerException("the name can not be null or empty");
        }

        if (this.nameSourceMap.containsKey(name)) {         //local cache
            return this.nameSourceMap.get(name);
        } else {                                            //remote data then save to local cache
            Object[] params = new Object[]{name};
            Object responseObj = this.innerRpcRequest("getSourceByName",
                                                      params);
            if (responseObj != null) {
                Source source = GSON.fromJson(responseObj.toString(), Source.class);
                this.nameSourceMap.put(name, source);
                return source;
            } else {
                throw new RuntimeException("can not get source info!");
            }
        }
    }

    public Sink getSinkBySecret(String secret) {
        if (Strings.isNullOrEmpty(secret)) {
            throw new NullPointerException("the secret can not be null or empty");
        }

        if (this.secretSinkMap.containsKey(secret)) {       //local cache
            return this.secretSinkMap.get(secret);
        } else {                                            //remote data then save to local cache
            Object[] params = new Object[]{secret};
            Object responseObj = this.innerRpcRequest("getSinkBySecret",
                                                      params);
            if (responseObj != null) {
                Sink sink = GSON.fromJson(responseObj.toString(), Sink.class);
                this.secretSinkMap.put(secret, sink);
                return sink;
            } else {
                throw new RuntimeException("can not get sink info!");
            }
        }
    }

    public Sink getSinkByName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            throw new NullPointerException("the name can not be null or empty");
        }

        if (this.nameSinkMap.containsKey(name)) {           //local cache
            return this.nameSinkMap.get(name);
        } else {                                            //remote data then save to local cache
            Object[] params = new Object[]{name};
            Object responseObj = this.innerRpcRequest("getSinkByName",
                                                      params);
            if (responseObj != null) {
                Sink sink = GSON.fromJson(responseObj.toString(), Sink.class);
                this.nameSinkMap.put(name, sink);
                return sink;
            } else {
                throw new RuntimeException("can not get sink info!");
            }
        }
    }

    public Stream getStreamByToken(String token) {
        if (Strings.isNullOrEmpty(token)) {
            throw new NullPointerException("the token can not be null or empty");
        }

        if (this.streamMap.containsKey(token)) {   //local cache
            return this.streamMap.get(token);
        } else {                                            //remote data then save to local cache
            Object[] params = new Object[]{token};
            Object responseObj = this.innerRpcRequest("getStreamByToken",
                                                      params);
            if (responseObj != null) {
                Stream stream = GSON.fromJson(responseObj.toString(), Stream.class);
                this.streamMap.put(token, stream);
                return stream;
            } else {
                throw new RuntimeException("can not get token info!");
            }
        }
    }

    public class InnerEventProcessor {

        @Subscribe
        public void onInnerEvent(InnerEvent innerEvent) {
            Message msg = innerEvent.getMsg();
            String jsonStr = new String(msg.getContent());
            logger.info("received inner event , content : " + jsonStr);

            //TODO:拆分三个path监听事件,判断到底刷新哪个
            refreshSourceCache();

            refreshSinkCache();

            refreshStreamCache();
        }
    }

    private void refreshSourceCache() {
        for (String secret : this.secretSourceMap.keySet()) {
            Object[] params = new Object[]{secret};
            Object responseObj = this.innerRpcRequest("getSourceBySecret",
                                                      params);
            if (responseObj != null) {
                Source source = GSON.fromJson(responseObj.toString(), Source.class);
                this.secretSourceMap.put(secret, source);
            } else {
                throw new RuntimeException("can not get source info!");
            }
        }
    }

    private void refreshSinkCache() {
        for (String secret : this.secretSinkMap.keySet()) {
            Object[] params = new Object[]{secret};
            Object responseObj = this.innerRpcRequest("getSinkBySecret",
                                                      params);
            if (responseObj != null) {
                Sink sink = GSON.fromJson(responseObj.toString(), Sink.class);
                this.secretSinkMap.put(secret, sink);
            } else {
                throw new RuntimeException("can not get sink info!");
            }
        }
    }

    private void refreshStreamCache() {
        for (String token : this.streamMap.keySet()) {
            Object[] params = new Object[]{token};
            Object responseObj = this.innerRpcRequest("getStreamByToken",
                                                      params);
            if (responseObj != null) {
                Stream stream = GSON.fromJson(responseObj.toString(), Stream.class);
                this.streamMap.put(token, stream);
            } else {
                throw new RuntimeException("can not get token info!");
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

    public static class Source {

        private String secret;
        private String name;
        private String type;
        private String appId;
        private String broadcastable;
        private String routingKey;

        public Source() {
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getBroadcastable() {
            return broadcastable;
        }

        public void setBroadcastable(String broadcastable) {
            this.broadcastable = broadcastable;
        }

        public String getRoutingKey() {
            return routingKey;
        }

        public void setRoutingKey(String routingKey) {
            this.routingKey = routingKey;
        }
    }

    public static class Sink {
        private String secret;
        private String name;
        private String queueName;
        private String routingKey;
        private String type;
        private String appId;
        private String msgBodySize;

        public Sink() {
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getQueueName() {
            return queueName;
        }

        public void setQueueName(String queueName) {
            this.queueName = queueName;
        }

        public String getRoutingKey() {
            return routingKey;
        }

        public void setRoutingKey(String routingKey) {
            this.routingKey = routingKey;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getAppId() {
            return appId;
        }

        public void setAppId(String appId) {
            this.appId = appId;
        }

        public String getMsgBodySize() {
            return msgBodySize;
        }

        public void setMsgBodySize(String msgBodySize) {
            this.msgBodySize = msgBodySize;
        }
    }

    public static class Stream {
        private String sourceSecret;
        private String sourceName;
        private String sinkSecret;
        private String sinkName;
        private String token;

        public Stream() {
        }

        public String getSourceSecret() {
            return sourceSecret;
        }

        public void setSourceSecret(String sourceSecret) {
            this.sourceSecret = sourceSecret;
        }

        public String getSourceName() {
            return sourceName;
        }

        public void setSourceName(String sourceName) {
            this.sourceName = sourceName;
        }

        public String getSinkSecret() {
            return sinkSecret;
        }

        public void setSinkSecret(String sinkSecret) {
            this.sinkSecret = sinkSecret;
        }

        public String getSinkName() {
            return sinkName;
        }

        public void setSinkName(String sinkName) {
            this.sinkName = sinkName;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

}
