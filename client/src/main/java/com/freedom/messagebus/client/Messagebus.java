package com.freedom.messagebus.client;

import com.freedom.messagebus.client.carry.*;
import com.freedom.messagebus.client.carry.impl.*;
import com.freedom.messagebus.client.message.model.Message;
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

    public Messagebus(String appId) {
        super(appId);

        producer = new GenericProducer();
        consumer = new GenericConsumer();
        publisher = new GenericPublisher();
        subscriber = new GenericSubscriber();
        requester = new GenericRequester();
        responser = new GenericResponser();
        broadcaster = new GenericBroadcaster();
    }

    @Override
    public void produce(Message msg, String to) {
        producer.setContext(context);
        producer.produce(msg, to);
    }

    @Override
    public void produceWithTX(Message msg, String to) {
        producer.setContext(context);
        producer.produceWithTX(msg, to);
    }

    @Override
    public void batchProduce(Message[] msgs, String to) {
        producer.setContext(context);
        producer.batchProduce(msgs, to);
    }

    @Override
    public void batchProduceWithTX(Message[] msgs, String to) {
        producer.setContext(context);
        producer.batchProduceWithTX(msgs, to);
    }

    @Override
    public void consume(IMessageReceiveListener onMessage, long timeout, TimeUnit unit) {
        consumer.setContext(context);
        consumer.consume(onMessage, timeout, unit);
    }

    @Override
    public List<Message> consume(int expectedNum) {
        consumer.setContext(context);
        return consumer.consume(expectedNum);
    }

    @Override
    public Message request(Message msg, String to, long timeout) throws MessageResponseTimeoutException {
        requester.setContext(context);
        return requester.request(msg, to, timeout);
    }

    @Override
    public void responseTmpMessage(Message msg, String queueName) {
        responser.setContext(context);
        responser.responseTmpMessage(msg, queueName);
    }

    @Override
    public void publish(Message[] msgs) {
        publisher.setContext(context);
        publisher.publish(msgs);
    }

    @Override
    public void subscribe(IMessageReceiveListener onMessage, List<String> subQueues, long timeout, TimeUnit unit) {
        subscriber.setContext(context);
        subscriber.subscribe(onMessage, subQueues, timeout, unit);
    }

    @Override
    public void broadcast(Message[] msgs) {
        broadcaster.setContext(context);
        broadcaster.broadcast(msgs);
    }
}
