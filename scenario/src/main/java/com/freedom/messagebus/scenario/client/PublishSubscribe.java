package com.freedom.messagebus.scenario.client;

import com.freedom.messagebus.client.IMessageReceiveListener;
import com.freedom.messagebus.client.IPublisher;
import com.freedom.messagebus.client.Messagebus;
import com.freedom.messagebus.client.MessagebusConnectedFailedException;
import com.freedom.messagebus.client.impl.GenericSubscriber;
import com.freedom.messagebus.client.message.model.*;
import com.freedom.messagebus.common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 2/24/15.
 */
public class PublishSubscribe {

    private static final Log logger = LogFactory.getLog(PublishSubscribe.class);

    private static final String host = "127.0.0.1";
    private static final int    port = 6379;

    public static void main(String[] args) {
        publish();

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        subscribe();
    }

    private static void publish() {
        //crm
        String appid = "djB5l1n7PbFsszF5817JOon2895El1KP";
        Messagebus client = Messagebus.createClient(appid);
        //set zookeeper info
        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

        try {
            client.open();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        }

        Message msg = MessageFactory.createMessage(MessageType.PubSubMessage);
        msg.getMessageHeader().setContentType("text/plain");
        msg.getMessageHeader().setContentEncoding("utf-8");

        PubSubMessage.PubSubMessageBody body = new PubSubMessage.PubSubMessageBody();
        body.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));

        msg.setMessageBody(body);

        IPublisher publisher = client.getPublisher();
        publisher.publish(new Message[] {msg});

        client.close();
    }

    private static void subscribe() {
        //erp
        String appid = "D0fW8u2u1v7S1IvI8qoQg3dUlLL5b36q";
        Messagebus client = Messagebus.createClient(appid);
        //set zookeeper info
        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

        try {
            client.open();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        }

        GenericSubscriber subscriber = client.getSubscriber();
        subscriber.setOnMessage(new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {
                logger.info(message.getMessageHeader().getMessageId());
            }
        });

        List<String> subscribeQueues = new ArrayList<>(1);
        subscribeQueues.add("crm");

        subscriber.setSubQueueNames(subscribeQueues);
        subscriber.setTimeout(10);

        subscriber.startup();

        client.close();
    }

}
