package com.messagebus.scenario.client;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
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
        MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
        Messagebus client = singlePool.getResource();

        Message msg = MessageFactory.createMessage(MessageType.QueueMessage);
        msg.setContentType("text/plain");
        msg.setContentEncoding("utf-8");
        msg.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));

        client.produce(secret, "emapDemoConsume", msg, token);

        singlePool.returnResource(client);
        singlePool.destroy();
    }

    private static void consumeWithPullStyle() {
        String secret = "zxdjnflakwenklasjdflkqpiasdfnj";
        MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
        Messagebus client = singlePool.getResource();

        List<Message> msgs = client.consume(secret, 1);

        for (Message msg : msgs) {
            logger.info(msg.getMessageId());
        }

        singlePool.returnResource(client);
        singlePool.destroy();
    }

    private static void ConsumeWithPushStyle() {
        String secret = "zxdjnflakwenklasjdflkqpiasdfnj";
        MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
        Messagebus client = singlePool.getResource();

        client.consume(secret, 2, TimeUnit.SECONDS, new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {
                logger.info(message.getMessageId());
            }
        });

        singlePool.returnResource(client);
        singlePool.destroy();
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

        private Thread               currentThread;
        private MessagebusSinglePool singlePool;
        private Messagebus           client;

        public AsyncConsumeThread() {
            this.currentThread = new Thread(this);
            this.currentThread.setName("AsyncConsumeThread");
            this.currentThread.setDaemon(true);
        }

        @Override
        public void run() {
            String secret = "zxdjnflakwenklasjdflkqpiasdfnj";
            singlePool = new MessagebusSinglePool(host, port);
            client = singlePool.getResource();

            //register notification listener
            client.setNotificationListener(new IMessageReceiveListener() {
                @Override
                public void onMessage(Message message) {
                    logger.info("received notification : " + message.getAppId());
                }
            });

            //long long time
            client.consume(secret, Integer.MAX_VALUE, TimeUnit.SECONDS, new IMessageReceiveListener() {
                @Override
                public void onMessage(Message message) {
                    logger.info(message.getMessageId());
                }
            });
        }

        public void startup() {
            this.currentThread.start();
        }

        public void shutdown() {
            singlePool.returnResource(client);
            singlePool.destroy();
            this.currentThread.interrupt();
        }
    }

}
