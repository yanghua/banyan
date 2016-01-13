package com.messagebus.client;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.util.HashMap;
import java.util.Map;

/**
 * the config manager
 */
public class ConfigManager {

    private static final Log    logger            = LogFactory.getLog(ConfigManager.class);
    private static final Gson   GSON              = new Gson();
    private static final String SOURCE_EVENT_TYPE = "source";
    private static final String SINK_EVENT_TYPE   = "sink";
    private static final String STREAM_EVENT_TYPE = "stream";

    private static final String REVERSE_MESSAGE_ZK_PATH               = "/reverse/message";
    private static final String REVERSE_MESSAGE_SOURCE_ZK_PATH        = REVERSE_MESSAGE_ZK_PATH + "/source";
    private static final String REVERSE_MESSAGE_SINK_ZK_PATH          = REVERSE_MESSAGE_ZK_PATH + "/sink";
    private static final String REVERSE_MESSAGE_STREAM_ZK_PATH        = REVERSE_MESSAGE_ZK_PATH + "/stream";
    private static final String REVERSE_MESSAGE_SOURCE_SECRET_ZK_PATH = REVERSE_MESSAGE_SOURCE_ZK_PATH + "/secret";
    private static final String REVERSE_MESSAGE_SOURCE_NAME_ZK_PATH   = REVERSE_MESSAGE_SOURCE_ZK_PATH + "/name";
    private static final String REVERSE_MESSAGE_SINK_SECRET_ZK_PATH   = REVERSE_MESSAGE_SINK_ZK_PATH + "/secret";
    private static final String REVERSE_MESSAGE_SINK_NAME_ZK_PATH     = REVERSE_MESSAGE_SINK_ZK_PATH + "/name";
    private static final String REVERSE_MESSAGE_STREAM_TOKEN_ZK_PATH  = REVERSE_MESSAGE_STREAM_ZK_PATH + "/token";

    private Map<String, Source> secretSourceMap = new HashMap<String, Source>();
    private Map<String, Source> nameSourceMap   = new HashMap<String, Source>();
    private Map<String, Sink>   secretSinkMap   = new HashMap<String, Sink>();
    private Map<String, Sink>   nameSinkMap     = new HashMap<String, Sink>();
    private Map<String, Stream> streamMap       = new HashMap<String, Stream>();

    private CuratorFramework openedZookeeper;
    private EventBus         componentEventBus;

    public ConfigManager(CuratorFramework zookeeper) {
        this.openedZookeeper = zookeeper;

        //source -> secret
        PathChildrenCache sourceSecretCache = new PathChildrenCache(zookeeper,
                REVERSE_MESSAGE_SOURCE_SECRET_ZK_PATH, false);
        sourceSecretCache.getListenable().addListener(new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework curatorFramework,
                                   PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                onPathChildrenChanged(REVERSE_MESSAGE_SOURCE_SECRET_ZK_PATH);
            }
        });

        //source -> name
        PathChildrenCache souceNameCache = new PathChildrenCache(zookeeper,
                REVERSE_MESSAGE_SOURCE_NAME_ZK_PATH, false);
        souceNameCache.getListenable().addListener(new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework curatorFramework,
                                   PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                onPathChildrenChanged(REVERSE_MESSAGE_SOURCE_NAME_ZK_PATH);
            }
        });

        //sink -> secret
        PathChildrenCache sinkSecretCache = new PathChildrenCache(zookeeper,
                REVERSE_MESSAGE_SINK_SECRET_ZK_PATH, false);
        sinkSecretCache.getListenable().addListener(new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework curatorFramework,
                                   PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                onPathChildrenChanged(REVERSE_MESSAGE_SINK_SECRET_ZK_PATH);
            }
        });

        //sink -> name
        PathChildrenCache sinkNameCache = new PathChildrenCache(zookeeper,
                REVERSE_MESSAGE_SINK_NAME_ZK_PATH, false);
        sinkNameCache.getListenable().addListener(new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                onPathChildrenChanged(REVERSE_MESSAGE_SINK_NAME_ZK_PATH);
            }
        });

        //stream -> token
        PathChildrenCache streamTokenCache = new PathChildrenCache(zookeeper,
                REVERSE_MESSAGE_STREAM_TOKEN_ZK_PATH, false);
        streamTokenCache.getListenable().addListener(new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent pathChildrenCacheEvent) throws Exception {
                onPathChildrenChanged(REVERSE_MESSAGE_STREAM_TOKEN_ZK_PATH);
            }
        });
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
            String sourceStr = getDataFromZK(REVERSE_MESSAGE_SOURCE_SECRET_ZK_PATH + "/" + secret);
            Source source    = GSON.fromJson(sourceStr, Source.class);
            this.secretSourceMap.put(secret, source);
            return source;
        }
    }

    public Source getSourceByName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            throw new NullPointerException("the name can not be null or empty");
        }

        if (this.nameSourceMap.containsKey(name)) {         //local cache
            return this.nameSourceMap.get(name);
        } else {                                            //remote data then save to local cache
            String sourceJson = getDataFromZK(REVERSE_MESSAGE_SOURCE_NAME_ZK_PATH + "/" + name);
            Source source     = GSON.fromJson(sourceJson, Source.class);
            this.nameSourceMap.put(name, source);
            return source;
        }
    }

    public Sink getSinkBySecret(String secret) {
        if (Strings.isNullOrEmpty(secret)) {
            throw new NullPointerException("the secret can not be null or empty");
        }

        if (this.secretSinkMap.containsKey(secret)) {       //local cache
            return this.secretSinkMap.get(secret);
        } else {                                            //remote data then save to local cache
            String sinkJson = getDataFromZK(REVERSE_MESSAGE_SINK_SECRET_ZK_PATH + "/" + secret);
            Sink   sink     = GSON.fromJson(sinkJson, Sink.class);
            this.secretSinkMap.put(secret, sink);
            return sink;
        }
    }

    public Sink getSinkByName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            throw new NullPointerException("the name can not be null or empty");
        }

        if (this.nameSinkMap.containsKey(name)) {           //local cache
            return this.nameSinkMap.get(name);
        } else {                                            //remote data then save to local cache
            String sinkJson = getDataFromZK(REVERSE_MESSAGE_SINK_NAME_ZK_PATH + "/" + name);
            Sink   sink     = GSON.fromJson(sinkJson, Sink.class);
            this.nameSinkMap.put(name, sink);
            return sink;
        }
    }

    public Stream getStreamByToken(String token) {
        if (Strings.isNullOrEmpty(token)) {
            throw new NullPointerException("the token can not be null or empty");
        }

        if (this.streamMap.containsKey(token)) {            //local cache
            return this.streamMap.get(token);
        } else {                                            //remote data then save to local cache
            String streamJson = getDataFromZK(REVERSE_MESSAGE_STREAM_ZK_PATH + "/" + token);
            Stream stream     = GSON.fromJson(streamJson, Stream.class);
            this.streamMap.put(token, stream);
            return stream;
        }
    }

    private void refreshSourceCache(boolean bySecret) {
        if (bySecret) {
            for (String secret : this.secretSourceMap.keySet()) {
                String sourceStr = getDataFromZK(REVERSE_MESSAGE_SOURCE_SECRET_ZK_PATH + "/" + secret);
                Source source    = GSON.fromJson(sourceStr, Source.class);
                this.secretSourceMap.put(secret, source);
            }
        } else {
            for (String name : this.nameSourceMap.keySet()) {
                String sourceJson = getDataFromZK(REVERSE_MESSAGE_SOURCE_NAME_ZK_PATH + "/" + name);
                Source source     = GSON.fromJson(sourceJson, Source.class);
                this.nameSourceMap.put(name, source);
            }
        }
    }

    private void refreshSinkCache(boolean bySecret) {
        if (bySecret) {
            for (String secret : this.secretSinkMap.keySet()) {
                String sinkJson = getDataFromZK(REVERSE_MESSAGE_SINK_SECRET_ZK_PATH + "/" + secret);
                Sink   sink     = GSON.fromJson(sinkJson, Sink.class);
                this.secretSinkMap.put(secret, sink);
            }
        } else {
            for (String name : this.nameSinkMap.keySet()) {
                String sinkJson = getDataFromZK(REVERSE_MESSAGE_SINK_NAME_ZK_PATH + "/" + name);
                Sink   sink     = GSON.fromJson(sinkJson, Sink.class);
                this.nameSinkMap.put(name, sink);
            }
        }
    }

    private void refreshStreamCache() {
        for (String token : this.streamMap.keySet()) {
            String streamJson = getDataFromZK(REVERSE_MESSAGE_STREAM_ZK_PATH + "/" + token);
            Stream stream     = GSON.fromJson(streamJson, Stream.class);
            this.streamMap.put(token, stream);
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
        private String autoAck;
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

        public boolean isAutoAck() {
            return autoAck == "1";
        }

        public void setAutoAck(String autoAck) {
            this.autoAck = autoAck;
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

    private String getDataFromZK(String path) {
        try {
            logger.debug("path : " + path);
            return new String(openedZookeeper.getData().forPath(path));
        } catch (Exception e) {
            logger.error(e);
        }

        return "";
    }

    private void onPathChildrenChanged(String path) {
        logger.debug("received path change from zookeeper, key : " + path);
        String partPath = path.replace(REVERSE_MESSAGE_ZK_PATH + "/", "");

        if (partPath.startsWith(SOURCE_EVENT_TYPE)) {
            boolean refreshBySecret = partPath.endsWith("secret");
            refreshSourceCache(refreshBySecret);
        } else if (partPath.startsWith(SINK_EVENT_TYPE)) {
            boolean refreshBySecret = partPath.endsWith("secret");
            refreshSinkCache(refreshBySecret);
        } else if (partPath.startsWith(STREAM_EVENT_TYPE)) {
            refreshStreamCache();
        } else {
            logger.warn("received unknown event type : " + partPath);
        }

    }

}
