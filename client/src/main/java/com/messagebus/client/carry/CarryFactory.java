package com.messagebus.client.carry;

import com.messagebus.client.GenericContext;

/**
 * Created by yanghua on 4/2/15.
 */
public class CarryFactory {

    public static IBroadcaster createBroadcaster(GenericContext context) {
        GenericBroadcaster broadcaster = new GenericBroadcaster();
        broadcaster.setContext(context);

        return broadcaster;
    }

    public static IConsumer createConsumer(GenericContext context) {
        GenericConsumer consumer = new GenericConsumer();
        consumer.setContext(context);

        return consumer;
    }

    public static IProducer createProducer(GenericContext context) {
        GenericProducer producer = new GenericProducer();
        producer.setContext(context);

        return producer;
    }

    public static IPublisher createPublisher(GenericContext context) {
        GenericPublisher publisher = new GenericPublisher();
        publisher.setContext(context);

        return publisher;
    }

    public static IRequester createRequester(GenericContext context) {
        GenericRequester requester = new GenericRequester();
        requester.setContext(context);

        return requester;
    }

    public static IResponser createResponser(GenericContext context) {
        GenericResponser responser = new GenericResponser();
        responser.setContext(context);

        return responser;
    }

    public static IRpcRequester createRpcRequester(GenericContext context) {
        GenericRpcRequester rpcRequester = new GenericRpcRequester();
        rpcRequester.setContext(context);

        return rpcRequester;
    }

    public static IRpcResponser createRpcResponser(GenericContext context) {
        GenericRpcResponser rpcResponser = new GenericRpcResponser();
        rpcResponser.setContext(context);

        return rpcResponser;
    }

    public static ISubscriber createSubscriber(GenericContext context) {
        GenericSubscriber subscriber = new GenericSubscriber();
        subscriber.setContext(context);

        return subscriber;
    }

}
