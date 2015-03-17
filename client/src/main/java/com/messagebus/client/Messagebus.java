package com.messagebus.client;

import com.messagebus.client.message.model.Message;
import com.messagebus.client.carry.*;
import com.messagebus.client.carry.impl.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * the main operator of messagebus client
 */
public class Messagebus extends InnerClient implements IProducer, IConsumer,
                                                       IRequester, IResponser,
                                                       IPublisher, ISubscriber,
                                                       IBroadcaster {

    private static final Log logger = LogFactory.getLog(Messagebus.class);

    private GenericProducer    producer;
    private GenericConsumer    consumer;
    private GenericPublisher   publisher;
    private GenericSubscriber  subscriber;
    private GenericRequester   requester;
    private GenericResponser   responser;
    private GenericBroadcaster broadcaster;

    public Messagebus() {
        super();

        producer = new GenericProducer();
        consumer = new GenericConsumer();
        publisher = new GenericPublisher();
        subscriber = new GenericSubscriber();
        requester = new GenericRequester();
        responser = new GenericResponser();
        broadcaster = new GenericBroadcaster();
    }

    @Override
    public void produce(String secret, String to, Message msg, String token) {
        producer.setContext(context);
        producer.produce(secret, to, msg, token);
    }

    @Override
    public void produceWithTX(String secret, String to, Message msg, String token) {
        producer.setContext(context);
        producer.produceWithTX(secret, to, msg, token);
    }

    @Override
    public void batchProduce(String secret, String to, Message[] msgs, String token) {
        producer.setContext(context);
        producer.batchProduce(secret, to, msgs, token);
    }

    @Override
    public void batchProduceWithTX(String secret, String to, Message[] msgs, String token) {
        producer.setContext(context);
        producer.batchProduceWithTX(secret, to, msgs, token);
    }

    @Override
    public void consume(String secret, long timeout, TimeUnit unit, IMessageReceiveListener onMessage) {
        consumer.setContext(context);
        consumer.consume(secret, timeout, unit, onMessage);
    }

    @Override
    public List<Message> consume(String secret, int expectedNum) {
        consumer.setContext(context);
        return consumer.consume(secret, expectedNum);
    }

    @Override
    public Message request(String secret, String to, Message msg, String token, long timeout) throws MessageResponseTimeoutException {
        requester.setContext(context);
        return requester.request(secret, to, msg, token, timeout);
    }

    @Override
    public void response(String secret, IRequestListener requestListener, long timeout, TimeUnit timeUnit) {
        responser.setContext(context);
        responser.response(secret, requestListener, timeout, timeUnit);
    }

    @Override
    public void publish(String secret, Message[] msgs, String token) {
        publisher.setContext(context);
        publisher.publish(secret, msgs, token);
    }

    @Override
    public void subscribe(String secret, IMessageReceiveListener onMessage, long timeout, TimeUnit unit) {
        subscriber.setContext(context);
        subscriber.subscribe(secret, onMessage, timeout, unit);
    }

    @Override
    public void broadcast(String secret, Message[] msgs, String token) {
        broadcaster.setContext(context);
        broadcaster.broadcast(secret, msgs, token);
    }

    public void setNotificationListener(IMessageReceiveListener notificationListener) {
        this.notificationListener = notificationListener;
        context.setNoticeListener(this.notificationListener);
    }
}
