package com.messagebus.client;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.messagebus.client.event.component.ClientDestroyEventProcessor;
import com.messagebus.client.event.component.ClientInitedEventProcessor;
import com.messagebus.client.event.component.InnerEvent;
import com.messagebus.client.event.component.NoticeEvent;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.common.RandomHelper;
import com.messagebus.interactor.proxy.ProxyConsumer;
import com.rabbitmq.client.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

/**
 * Created by yanghua on 3/18/15.
 */
public class MessagebusPool {

    private static final Log logger = LogFactory.getLog(MessagebusPool.class);

    private static final String INNER_EXCHANGE_NAME        = "exchange.proxy.message.inner";
    private static final String EVENT_ROUTING_KEY_NAME     = "routingkey.proxy.message.inner.event";
    private static final String NOTICE_ROUTING_KEY_NAME    = "routingkey.proxy.message.inner.notice";
    private static final String EVENT_QUEUE_NAME_PREFIX    = "autodelete_exclusive.queue.proxy.message.inner.event.";
    private static final String NOTICE_QUEUE_NAME_PREFIX   = "autodelete_exclusive.queue.proxy.message.inner.notice.";
    private static final String EVENT_CONSUMER_TAG_PREFIX  = "tag.proxy.message.inner.event.";
    private static final String NOTICE_CONSUMER_TAG_PREFIX = "tag.proxy.message.inner.notice.";

    private String                   host;
    private int                      port;
    private ConfigManager            configManager;
    private Connection               connection;
    private Channel                  innerChannel;
    private EventBus                 componentEventBus;
    private RemoteInnerEventListener remoteInnerEventListener;
    private RemoteNoticeListener     remoteNoticeListener;

    protected InnerPool innerPool;

    public MessagebusPool(String host, int port, GenericObjectPoolConfig poolConfig) {
        this.host = host;
        this.port = port;
        this.init();
        this.innerPool = new InnerPool(poolConfig,
                                       host,
                                       port,
                                       configManager,
                                       connection,
                                       componentEventBus);
    }

    public void registerComponentEventListener(Object listener) {
        this.componentEventBus.register(listener);
    }

    public Messagebus getResource() {
        return this.innerPool.getResource();
    }

    public void returnResource(Messagebus client) {
        this.innerPool.returnResource(client);
    }

    public void destroy() {
        this.remoteInnerEventListener.shutdown();
        this.remoteNoticeListener.shutdown();

        this.innerPool.destroy();

        try {
            if (this.innerChannel != null && this.innerChannel.isOpen()) {
                this.innerChannel.close();
            }

            if (this.connection != null && this.connection.isOpen()) {
                this.connection.close();
            }
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    protected void init() {
        this.componentEventBus = new AsyncEventBus("componentEventBus", Executors.newSingleThreadExecutor());

        this.configManager = new ConfigManager(this.host, this.port);
        this.configManager.setComponentEventBus(this.componentEventBus);

        this.registerComponentEventProcessor();

        ConnectionFactory connectionFactory = null;
        try {
            connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(this.host);
            connectionFactory.setPort(this.port);

            connectionFactory.setAutomaticRecoveryEnabled(true);
            connectionFactory.setTopologyRecoveryEnabled(true);
            connectionFactory.setConnectionTimeout(60000);
            connectionFactory.setRequestedHeartbeat(10);

            this.connection = connectionFactory.newConnection();
            this.innerChannel = this.connection.createChannel();

        } catch (IOException e) {
            logger.error("init message pool exception with host : " + connectionFactory.getHost()
                             + " and port : " + connectionFactory.getPort(), e);
            throw new RuntimeException("init message pool exception with host : " + connectionFactory.getHost()
                                           + " and port : " + connectionFactory.getPort(), e);
        } catch (TimeoutException e) {
            logger.error("init message pool exception with host : " + connectionFactory.getHost()
                             + " and port : " + connectionFactory.getPort(), e);
            throw new RuntimeException("init message pool exception with host : " + connectionFactory.getHost()
                                           + " and port : " + connectionFactory.getPort(), e);
        } catch (Exception e) {
            logger.error("init message pool exception with host : " + connectionFactory.getHost()
                             + " and port : " + connectionFactory.getPort(), e);
            throw new RuntimeException("init message pool exception with host : " + connectionFactory.getHost()
                                           + " and port : " + connectionFactory.getPort(), e);
        }

        this.remoteInnerEventListener = new RemoteInnerEventListener();
        this.remoteNoticeListener = new RemoteNoticeListener();

        //start listen remote event
        this.remoteInnerEventListener.start();
        this.remoteNoticeListener.start();
    }

    private void registerComponentEventProcessor() {
        this.componentEventBus.register(new ClientDestroyEventProcessor());
        this.componentEventBus.register(new ClientInitedEventProcessor());
        this.componentEventBus.register(configManager.new InnerEventProcessor());
    }

    private class RemoteInnerEventListener implements Runnable {

        private Thread currentThread;

        public RemoteInnerEventListener() {
            this.currentThread = new Thread(this);
            this.currentThread.setDaemon(true);
            this.currentThread.setName("remote inner event listener");
        }

        @Override
        public void run() {
            try {
                String queueName = EVENT_QUEUE_NAME_PREFIX + RandomHelper.randomNumberAndCharacter(5);
                //declare a non-durable auto-delete & exclusive queue then consume
                innerChannel.queueDeclare(queueName, false, true, true, null);

                Map<String, Object> matchHeader = new HashMap<String, Object>(2);
                matchHeader.put("x-match", "all");
                matchHeader.put("type", "event");
                innerChannel.queueBind(queueName, INNER_EXCHANGE_NAME, EVENT_ROUTING_KEY_NAME, matchHeader);

                String randomTag = RandomHelper.randomNumberAndCharacter(6);
                QueueingConsumer consumer = ProxyConsumer.consume(
                        innerChannel,
                        queueName,
                        true,
                        EVENT_CONSUMER_TAG_PREFIX + randomTag);
                while (true) {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                    final Message msg = MessageFactory.createMessage(delivery);

                    if (msg == null) continue;

                    InnerEvent innerEvent = new InnerEvent();
                    innerEvent.setMsg(msg);

                    logger.info("message bus pool received remote event !");

                    componentEventBus.post(innerEvent);
                }
            } catch (InterruptedException e) {
                logger.info(" message loop task interrupted!");
            } catch (ShutdownSignalException e) {
                logger.error(e);
            } catch (Exception e) {
                logger.error(e);
            }
        }

        public void start() {
            this.currentThread.start();
        }

        public void shutdown() {
            this.currentThread.interrupt();
        }
    }

    private class RemoteNoticeListener implements Runnable {

        private Thread currentThread;

        public RemoteNoticeListener() {
            this.currentThread = new Thread(this);
            this.currentThread.setDaemon(true);
            this.currentThread.setName("remote notice listener");
        }

        @Override
        public void run() {
            try {
                String queueName = NOTICE_QUEUE_NAME_PREFIX + RandomHelper.randomNumberAndCharacter(5);
                //declare a non-durable auto-delete & exclusive queue then consume
                innerChannel.queueDeclare(queueName, false, true, true, null);

                Map<String, Object> matchHeader = new HashMap<String, Object>(2);
                matchHeader.put("x-match", "all");
                matchHeader.put("type", "notice");
                innerChannel.queueBind(queueName, INNER_EXCHANGE_NAME, NOTICE_ROUTING_KEY_NAME, matchHeader);

                String randomTag = RandomHelper.randomNumberAndCharacter(6);
                QueueingConsumer consumer = ProxyConsumer.consume(
                        innerChannel,
                        queueName,
                        true,
                        NOTICE_CONSUMER_TAG_PREFIX + randomTag);
                while (true) {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();

                    final Message msg = MessageFactory.createMessage(delivery);

                    if (msg == null) continue;

                    NoticeEvent noticeEvent = new NoticeEvent();
                    noticeEvent.setMsg(msg);

                    componentEventBus.post(noticeEvent);
                }
            } catch (InterruptedException e) {
                logger.info(" message loop task interrupted!");
            } catch (ShutdownSignalException e) {
                logger.error(e);
            } catch (Exception e) {
                logger.error(e);
            }
        }

        public void start() {
            this.currentThread.start();
        }

        public void shutdown() {
            this.currentThread.interrupt();
        }

    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

}
