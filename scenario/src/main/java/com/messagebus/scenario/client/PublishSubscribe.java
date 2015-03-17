package com.messagebus.scenario.client;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusConnectedFailedException;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.client.message.model.PubSubMessage;
import com.messagebus.common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

        subscribe1();

        subscribe2();
    }

    private static void publish() {
        String secret = "oiqwenncuicnsdfuasdfnkajkwqowe";
        String token = "kjkjasdjfhkajsdfhksdjhfkasdf";
        Messagebus client = new Messagebus();
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

        client.publish(secret, new Message[]{msg}, token);

        client.close();
    }

    private static void subscribe1() {
        String secret = "nckljsenlkjanefluiwnlanfmsdfas";
        Messagebus client = new Messagebus();
        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

        try {
            client.open();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        }

        client.subscribe(secret, new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {
                logger.info(message.getMessageHeader().getMessageId());
            }
        }, 3, TimeUnit.SECONDS);

        client.close();
    }

    private static void subscribe2() {
        String secret = "zxcnvblawelkusahdfqwiuhowefhnx";

        Messagebus client = new Messagebus();
        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

        try {
            client.open();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        }

        client.subscribe(secret, new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {
                logger.info(message.getMessageHeader().getMessageId());
            }
        }, 3, TimeUnit.SECONDS);

        client.close();
    }

}
