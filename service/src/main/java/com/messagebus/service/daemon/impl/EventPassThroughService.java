package com.messagebus.service.daemon.impl;

import com.google.gson.Gson;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.common.Constants;
import com.messagebus.common.InnerEventEntity;
import com.messagebus.interactor.proxy.ProxyProducer;
import com.messagebus.service.daemon.DaemonService;
import com.messagebus.service.daemon.RunPolicy;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by yanghua on 10/28/15.
 */
@DaemonService(value = "eventPassThroughService", policy = RunPolicy.ONCE)
public class EventPassThroughService extends AbstractService {

    private static final Log logger = LogFactory.getLog(EventPassThroughService.class);

    private static final String EVENT_ROUTING_KEY_NAME = "routingkey.proxy.message.inner.#";
    private static final Gson   GSON                   = new Gson();

    private Connection connection;
    private Channel    mqChannel;

    public EventPassThroughService(Map<String, Object> context) {
        super(context);
    }

    @Override
    public void run() {
        logger.info("started event pass through service");
        String zkHost = this.context.get(com.messagebus.service.Constants.ZK_HOST_KEY).toString();
        int    zkPort = Integer.parseInt(this.context.get(com.messagebus.service.Constants.ZK_PORT_KEY).toString());

        RetryPolicy retryPolicy   = new ExponentialBackoffRetry(1000, 10);
        String      connectionStr = String.format("%s:%s", zkHost, zkPort);
        CuratorFramework zookeeper = CuratorFrameworkFactory.newClient(
                connectionStr, retryPolicy);

        try {
            zookeeper.start();

            String mbServerInfoJson = new String(zookeeper.getData().forPath(COMPONENT_MESSAGE_ZK_PATH));
            Map    mbHostAndPortObj = GSON.fromJson(mbServerInfoJson, Map.class);

            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(mbHostAndPortObj.get("mqHost").toString());
            connection = connectionFactory.newConnection();
            mqChannel = connection.createChannel();

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

            TimeUnit.MINUTES.sleep(Integer.MAX_VALUE);
        } catch (IOException e) {
            logger.error(e);
        } catch (TimeoutException e) {
            logger.error(e);
        } catch (InterruptedException e) {
            logger.error(e);
        } catch (Exception e) {
            logger.error(e);
        } finally {
            logger.info("event pass through service shut down...");
            try {
                if (this.mqChannel != null && this.mqChannel.isOpen()) {
                    this.mqChannel.close();
                }

                if (this.connection != null && this.connection.isOpen()) {
                    this.connection.close();
                }

                if (zookeeper.getState().equals(CuratorFrameworkState.STARTED)) {
                    zookeeper.close();
                }
            } catch (IOException e) {
                logger.error(e);
            } catch (TimeoutException e) {
                logger.error(e);
            } catch (Exception e) {
                logger.error(e);
            }
        }

    }

    public void onPathChildrenChanged(String path) {
        logger.info("received path change from zookeeper, key : " + path);
        try {
            String           secret      = path.replace(REVERSE_MESSAGE_ZK_PATH + "/", "");
            InnerEventEntity eventEntity = new InnerEventEntity();
            eventEntity.setIdentifier(secret);
            eventEntity.setValue("");
            eventEntity.setType("event");
            String jsonObjStr = GSON.toJson(eventEntity);

            Message             eventMsg = MessageFactory.createMessage();
            Map<String, Object> map      = new HashMap<String, Object>(1);
            map.put("type", "event");
            eventMsg.setHeaders(map);
            eventMsg.setContent(jsonObjStr.getBytes());
            AMQP.BasicProperties properties = MessageHeaderTransfer.box(eventMsg);

            ProxyProducer.produce(Constants.PROXY_EXCHANGE_NAME,
                    mqChannel,
                    EVENT_ROUTING_KEY_NAME,
                    eventMsg.getContent(),
                    properties);
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

}
