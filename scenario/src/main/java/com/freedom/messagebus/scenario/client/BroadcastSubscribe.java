package com.freedom.messagebus.scenario.client;

import com.freedom.messagebus.client.IMessageReceiveListener;
import com.freedom.messagebus.client.Messagebus;
import com.freedom.messagebus.client.MessagebusConnectedFailedException;
import com.freedom.messagebus.client.message.model.BroadcastMessage;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.model.MessageFactory;
import com.freedom.messagebus.client.message.model.MessageType;
import com.freedom.messagebus.common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BroadcastSubscribe {

    private static final Log    logger = LogFactory.getLog(BroadcastSubscribe.class);

    private static final String host   = "127.0.0.1";
    private static final int    port   = 6379;

    public static void main(String[] args) {
        broadcast();

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        subscribe1();

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        subscribe2();
    }

    private static void broadcast() {
        //crm
        String appid = "djB5l1n7PbFsszF5817JOon2895El1KP";
        Messagebus client = new Messagebus(appid);
        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

        try {
            client.open();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        }

        Message msg = MessageFactory.createMessage(MessageType.BroadcastMessage);
        msg.getMessageHeader().setContentType("text/plain");
        msg.getMessageHeader().setContentEncoding("utf-8");

        BroadcastMessage.BroadcastMessageBody body = new BroadcastMessage.BroadcastMessageBody();
        body.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));

        msg.setMessageBody(body);

        client.broadcast(new Message[] {msg});

        logger.info("crm broadcast!");
        client.close();
    }

    private static void subscribe1() {
        //erp
        String appid = "D0fW8u2u1v7S1IvI8qoQg3dUlLL5b36q";
        Messagebus client = new Messagebus(appid);
        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

        try {
            client.open();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        }

        logger.info("erp is subscribing message from crm! ");
        List<String> subscribeQueues = new ArrayList<>(1);
        subscribeQueues.add("crm");
        client.subscribe(new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {
                logger.info(message.getMessageHeader().getMessageId());
            }
        }, subscribeQueues, 10, TimeUnit.SECONDS);

        client.close();
    }

    private static void subscribe2() {
        //ucp
        String appid = "6vifQNkw225U6dS8cI92rS2eS1o7ZehQ";
        Messagebus client = new Messagebus(appid);
        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

        try {
            client.open();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        }

        logger.info("ucp is subscribing message from crm! ");
        List<String> subscribeQueues = new ArrayList<>(1);
        subscribeQueues.add("crm");
        client.subscribe(new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {
                logger.info(message.getMessageHeader().getMessageId());
            }
        }, subscribeQueues, 10, TimeUnit.SECONDS);

        client.close();
    }

}
