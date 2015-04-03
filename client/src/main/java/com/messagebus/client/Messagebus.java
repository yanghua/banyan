package com.messagebus.client;

import com.messagebus.client.carry.*;
import com.messagebus.client.message.model.Message;
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

    private IProducer    producer;
    private IConsumer    consumer;
    private IPublisher   publisher;
    private ISubscriber  subscriber;
    private IRequester   requester;
    private IResponser   responser;
    private IBroadcaster broadcaster;

    private Messagebus() {
        super();

        producer = CarryFactory.createProducer(context);
        consumer = CarryFactory.createConsumer(context);
        publisher = CarryFactory.createPublisher(context);
        subscriber = CarryFactory.createSubscriber(context);
        requester = CarryFactory.createRequester(context);
        responser = CarryFactory.createResponser(context);
        broadcaster = CarryFactory.createBroadcaster(context);
    }

    @Override
    public void produce(String secret, String to, Message msg, String token) {
        producer.produce(secret, to, msg, token);
    }

    @Override
    public void batchProduce(String secret, String to, Message[] msgs, String token) {
        producer.batchProduce(secret, to, msgs, token);
    }

    @Override
    public void consume(String secret, long timeout, TimeUnit unit, IMessageReceiveListener onMessage) {
        consumer.consume(secret, timeout, unit, onMessage);
    }

    @Override
    public List<Message> consume(String secret, int expectedNum) {
        return consumer.consume(secret, expectedNum);
    }

    @Override
    public Message request(String secret, String to, Message msg, String token, long timeout) throws MessageResponseTimeoutException {
        return requester.request(secret, to, msg, token, timeout);
    }

    @Override
    public void response(String secret, IRequestListener requestListener, long timeout, TimeUnit timeUnit) {
        responser.response(secret, requestListener, timeout, timeUnit);
    }

    @Override
    public void publish(String secret, Message[] msgs) {
        publisher.publish(secret, msgs);
    }

    @Override
    public void subscribe(String secret, IMessageReceiveListener onMessage, long timeout, TimeUnit unit) {
        subscriber.subscribe(secret, onMessage, timeout, unit);
    }

    @Override
    public void broadcast(String secret, Message[] msgs) {
        broadcaster.broadcast(secret, msgs);
    }

    public void setNotificationListener(IMessageReceiveListener notificationListener) {
        this.notificationListener = notificationListener;
        context.setNoticeListener(this.notificationListener);
    }
}
