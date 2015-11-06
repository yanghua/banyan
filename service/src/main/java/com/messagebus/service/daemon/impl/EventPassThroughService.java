package com.messagebus.service.daemon.impl;

import com.google.gson.Gson;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.common.Constants;
import com.messagebus.common.InnerEventEntity;
import com.messagebus.interactor.proxy.ProxyProducer;
import com.messagebus.interactor.pubsub.IPubSubListener;
import com.messagebus.interactor.pubsub.LongLiveZookeeper;
import com.messagebus.service.daemon.DaemonService;
import com.messagebus.service.daemon.RunPolicy;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
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
    private static final String REVERSE_MESSAGE_PATH      = "/reverse/message";
    private static final String COMPONENT_MESSAGE_ZK_PATH = "/component/message";


    private static final Gson GSON = new Gson();

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

        LongLiveZookeeper zookeeper = new LongLiveZookeeper(zkHost, zkPort);
        try {
            zookeeper.open();

            Map mbHostAndPortObj = zookeeper.get(COMPONENT_MESSAGE_ZK_PATH, Map.class);

            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(mbHostAndPortObj.get("mqHost").toString());
            connection = connectionFactory.newConnection();
            mqChannel = connection.createChannel();

            String[] paths = new String[]{REVERSE_MESSAGE_PATH};
            zookeeper.watch(paths, new EventChangedHandler());
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
        public void onChange(String channel, byte[] data, Map<String, Object> params) {
            logger.info("received node view change from pubsuber, key : " + channel);
            try {
                String secret = channel.replace(REVERSE_MESSAGE_PATH + "/", "");
                InnerEventEntity eventEntity = new InnerEventEntity();
                eventEntity.setIdentifier(secret);
                eventEntity.setValue(new String(data));
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
            } catch (Exception e) {
                logger.error(e);
            }
        }

    }

}
