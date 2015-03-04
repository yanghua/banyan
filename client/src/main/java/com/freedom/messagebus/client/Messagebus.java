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

    public Messagebus(String appId) {
        super(appId);
    }

    @Override
    public void produce(Message msg, String to) {
        GenericProducer producer = new GenericProducer();
        producer.setContext(context);
        producer.produce(msg, to);
    }

    @Override
    public void produceWithTX(Message msg, String to) {
        GenericProducer producer = new GenericProducer();
        producer.setContext(context);
        producer.produceWithTX(msg, to);
    }

    @Override
    public void batchProduce(Message[] msgs, String to) {
        GenericProducer producer = new GenericProducer();
        producer.setContext(context);
        producer.batchProduce(msgs, to);
    }

    @Override
    public void batchProduceWithTX(Message[] msgs, String to) {
        GenericProducer producer = new GenericProducer();
        producer.setContext(context);
        producer.batchProduceWithTX(msgs, to);
    }

    @Override
    public void consume(IMessageReceiveListener onMessage, long timeout, TimeUnit unit) {
        GenericConsumer consumer = new GenericConsumer();
        consumer.setContext(context);
        consumer.consume(onMessage, timeout, unit);
    }

    @Override
    public List<Message> consume(int expectedNum) {
        GenericConsumer consumer = new GenericConsumer();
        consumer.setContext(context);
        return consumer.consume(expectedNum);
    }

    @Override
    public Message request(Message msg, String to, long timeout) throws MessageResponseTimeoutException {
        GenericRequester requester = new GenericRequester();
        requester.setContext(context);
        return requester.request(msg, to, timeout);
    }

    @Override
    public void responseTmpMessage(Message msg, String queueName) {
        GenericResponser responser = new GenericResponser();
        responser.setContext(context);
        responser.responseTmpMessage(msg, queueName);
    }

    @Override
    public void publish(Message[] msgs) {
        GenericPublisher publisher = new GenericPublisher();
        publisher.setContext(context);
        publisher.publish(msgs);
    }

    @Override
    public void subscribe(IMessageReceiveListener onMessage, List<String> subQueues, long timeout, TimeUnit unit) {
        GenericSubscriber subscriber = new GenericSubscriber();
        subscriber.setContext(context);
        subscriber.subscribe(onMessage, subQueues, timeout, unit);
    }

    @Override
    public void broadcast(Message[] msgs) {
        GenericBroadcaster broadcaster = new GenericBroadcaster();
        broadcaster.setContext(context);
        broadcaster.broadcast(msgs);
    }
}
