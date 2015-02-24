package com.freedom.messagebus.scenario.client;

import com.freedom.messagebus.client.IMessageReceiveListener;
import com.freedom.messagebus.client.IProducer;
import com.freedom.messagebus.client.Messagebus;
import com.freedom.messagebus.client.MessagebusConnectedFailedException;
import com.freedom.messagebus.client.impl.AsyncConsumer;
import com.freedom.messagebus.client.impl.SyncConsumer;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.model.MessageFactory;
import com.freedom.messagebus.client.message.model.MessageType;
import com.freedom.messagebus.client.message.model.QueueMessage;
import com.freedom.messagebus.common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 2/23/15.
 */
public class ProduceConsume {

    private static final Log logger = LogFactory.getLog(ProduceConsume.class);

    private static final String host = "127.0.0.1";
    private static final int    port = 6379;

    public static void main(String[] args) {
        produce();

        //sleep
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        asyncConsume();

        //or
//        syncConsume();
    }

    private static void produce() {
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

        Message msg = MessageFactory.createMessage(MessageType.QueueMessage);
        msg.getMessageHeader().setContentType("text/plain");
        msg.getMessageHeader().setContentEncoding("utf-8");

        QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
        body.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));

        msg.setMessageBody(body);

        IProducer producer = client.getProducer();
        producer.produce(msg, "erp");

        client.close();
    }

    private static void syncConsume() {
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

        SyncConsumer syncConsumer = client.getSyncConsumer();
        List<Message> msgs = syncConsumer.consume(1);

        client.close();

        for (Message msg : msgs) {
            logger.info(msg.getMessageHeader().getMessageId());
        }
    }

    private static void asyncConsume() {
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

        AsyncConsumer asyncConsumer = client.getAsyncConsumer(new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {
                logger.info(message.getMessageHeader().getMessageId());
            }
        });

        asyncConsumer.setTimeout(5);
        asyncConsumer.startup();

        client.close();
    }

}
