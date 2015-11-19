package com.messagebus.service.daemon.impl;

import com.google.gson.Gson;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
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
import com.wisedu.astraea.configuration.IPubSubListener;
import com.wisedu.astraea.configuration.LongLiveZookeeper;
import com.wisedu.astraea.configuration.ZKEventType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    private static final String EVENT_ROUTING_KEY_NAME    = "routingkey.proxy.message.inner.#";
    private static final Gson GSON = new Gson();

    private LongLiveZookeeper zookeeper;
    private Connection connection;
    private Channel    mqChannel;

    public EventPassThroughService(Map<String, Object> context) {
        super(context);
    }

    @Override
    public void run() {
        logger.info("started event pass through service");
        String zkHost = this.context.get(com.messagebus.service.Constants.ZK_HOST_KEY).toString();
        int zkPort = Integer.parseInt(this.context.get(com.messagebus.service.Constants.ZK_PORT_KEY).toString());

        zookeeper = new LongLiveZookeeper(zkHost, zkPort);
        try {
            zookeeper.open();

            Map mbHostAndPortObj = zookeeper.get(COMPONENT_MESSAGE_ZK_PATH, Map.class);

            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(mbHostAndPortObj.get("mqHost").toString());
            connection = connectionFactory.newConnection();
            mqChannel = connection.createChannel();

            zookeeper.watch(REVERSE_MESSAGE_ZK_PATH, new EventChangedHandler());
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

                if (zookeeper.isAlive()) {
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

    public class EventChangedHandler implements IPubSubListener {

        @Override
        public void onChange(String channel, ZKEventType eventType) {
            logger.info("received node view change from zookeeper, key : " + channel);
            try {
                String secret = channel.replace(REVERSE_MESSAGE_ZK_PATH + "/", "");
                InnerEventEntity eventEntity = new InnerEventEntity();
                eventEntity.setIdentifier(secret);
                eventEntity.setValue("");
                eventEntity.setType("event");
                String jsonObjStr = GSON.toJson(eventEntity);

                Message eventMsg = MessageFactory.createMessage(MessageType.QueueMessage);
                Map<String, Object> map = new HashMap<String, Object>(1);
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

}
