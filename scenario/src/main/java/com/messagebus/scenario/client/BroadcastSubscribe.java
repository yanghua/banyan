package com.messagebus.scenario.client;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.message.model.IMessage;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;

public class BroadcastSubscribe {

    private static final Log logger = LogFactory.getLog(BroadcastSubscribe.class);

    private static final String host = "127.0.0.1";
    private static final int    port = 6379;

    public static void main(String[] args) {
        broadcast();

        subscribe1();

        subscribe2();
    }

    private static void broadcast() {
        String secret = "mucasdjfaskdufhqiiuuasdfasdnus";
        String token = "qiakdjfanekisdfadfhkqljwqheu";
        MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
        Messagebus client = singlePool.getResource();

        IMessage msg = MessageFactory.createMessage(MessageType.BroadcastMessage);
        msg.getMessageHeader().setContentType("text/plain");
        msg.getMessageHeader().setContentEncoding("utf-8");

        Message.MessageBody body = new Message.MessageBody();
        body.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));

        msg.setMessageBody(body);

        client.broadcast(secret, new IMessage[]{msg}, token);

        logger.info(" broadcast! ");

        singlePool.returnResource(client);
        singlePool.destroy();
    }

    private static void subscribe1() {
        String secret = "kjhasdfhlkuqjhgaebjhasgdfabfak";
        MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
        Messagebus client = singlePool.getResource();

        //notification handler
        client.setNotificationListener(new IMessageReceiveListener() {
            @Override
            public void onMessage(IMessage message) {
                logger.info("received notification !");
                logger.info(message.getMessageHeader().getMessageId());
            }
        });

        //business handler
        client.consume(secret, 3, TimeUnit.SECONDS, new IMessageReceiveListener() {
            @Override
            public void onMessage(IMessage message) {
                logger.info(message.getMessageHeader().getMessageId());
            }
        });

        singlePool.returnResource(client);
        singlePool.destroy();
    }

    private static void subscribe2() {
        String sercet = "zxdjnflakwenklasjdflkqpiasdfnj";
        MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
        Messagebus client = singlePool.getResource();

        client.setNotificationListener(new IMessageReceiveListener() {
            @Override
            public void onMessage(IMessage message) {
                logger.info("received notification !");
                logger.info(message.getMessageHeader().getMessageId());
            }
        });

        client.consume(sercet, 3, TimeUnit.SECONDS, new IMessageReceiveListener() {
            @Override
            public void onMessage(IMessage message) {
                logger.info(message.getMessageHeader().getMessageId());
            }
        });

        singlePool.returnResource(client);
        singlePool.destroy();
    }

}
