package com.messagebus.scenario.client;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
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
        MessagebusSinglePool singlePool = new MessagebusSinglePool(host);
        Messagebus client = singlePool.getResource();

        Message msg = MessageFactory.createMessage();
        msg.setContentType("text/plain");
        msg.setContentEncoding("utf-8");

        msg.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));

        client.broadcast(secret, new Message[]{msg});

        logger.info(" broadcast! ");

        singlePool.returnResource(client);
        singlePool.destroy();
    }

    private static void subscribe1() {
        String secret = "kjhasdfhlkuqjhgaebjhasgdfabfak";
        MessagebusSinglePool singlePool = new MessagebusSinglePool(host);
        Messagebus client = singlePool.getResource();

        //notification handler
        client.setNotificationListener(new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {
                logger.info("received notification !");
                logger.info(message.getMessageId());
            }
        });

        //business handler
        client.consume(secret, 3, TimeUnit.SECONDS, new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {
                logger.info(message.getMessageId());
            }
        });

        singlePool.returnResource(client);
        singlePool.destroy();
    }

    private static void subscribe2() {
        String sercet = "zxdjnflakwenklasjdflkqpiasdfnj";
        MessagebusSinglePool singlePool = new MessagebusSinglePool(host);
        Messagebus client = singlePool.getResource();

        client.setNotificationListener(new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {
                logger.info("received notification !");
                logger.info(message.getMessageId());
            }
        });

        client.consume(sercet, 3, TimeUnit.SECONDS, new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {
                logger.info(message.getMessageId());
            }
        });

        singlePool.returnResource(client);
        singlePool.destroy();
    }

}
