package com.messagebus.scenario.client;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusConnectedFailedException;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.client.message.model.QueueMessage;
import com.messagebus.common.Constants;
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

        ConsumeWithPushStyle();

        //or
        consumeWithPullStyle();

        //or async consume
        asyncConsume();
    }

    private static void produce() {
        String secret = "kljasdoifqoikjhhhqwhebasdfasdf";
        String token = "hlkasjdhfkqlwhlfalksjdhgssssas";
        Messagebus client = new Messagebus();
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

        client.produce(secret, "emapDemoConsume", msg, token);

        client.close();
    }

    private static void consumeWithPullStyle() {
        String secret = "zxdjnflakwenklasjdflkqpiasdfnj";
        Messagebus client = new Messagebus();
        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

        try {
            client.open();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        }

        List<Message> msgs = client.consume(secret, 1);

        client.close();

        for (Message msg : msgs) {
            logger.info(msg.getMessageHeader().getMessageId());
        }
    }

    private static void ConsumeWithPushStyle() {
        String secret = "zxdjnflakwenklasjdflkqpiasdfnj";
        Messagebus client = new Messagebus();
        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

        try {
            client.open();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        }

        client.consume(secret, 2, TimeUnit.SECONDS, new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {
                logger.info(message.getMessageHeader().getMessageId());
            }
        });

        client.close();
    }

    private static void asyncConsume() {
        AsyncConsumeThread asyncConsumeThread = new AsyncConsumeThread();
        asyncConsumeThread.startup();

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        asyncConsumeThread.shutdown();
    }

    private static class AsyncConsumeThread implements Runnable {

        private Thread currentThread;

        public AsyncConsumeThread() {
            this.currentThread = new Thread(this);
            this.currentThread.setName("AsyncConsumeThread");
            this.currentThread.setDaemon(true);
        }

        @Override
        public void run() {
            String secret = "zxdjnflakwenklasjdflkqpiasdfnj";
            Messagebus client = new Messagebus();
            client.setPubsuberHost(host);
            client.setPubsuberPort(port);

            //register notification listener
            client.setNotificationListener(new IMessageReceiveListener() {
                @Override
                public void onMessage(Message message) {
                    logger.info("received notification : " + message.getMessageHeader().getAppId());
                }
            });

            try {
                client.open();

                //long long time
                client.consume(secret, Integer.MAX_VALUE, TimeUnit.SECONDS, new IMessageReceiveListener() {
                    @Override
                    public void onMessage(Message message) {
                        logger.info(message.getMessageHeader().getMessageId());
                    }
                });
            } catch (MessagebusConnectedFailedException e) {
                e.printStackTrace();
            } finally {
                client.close();
            }
        }

        public void startup() {
            this.currentThread.start();
        }

        public void shutdown() {
            this.currentThread.interrupt();
        }
    }

}
