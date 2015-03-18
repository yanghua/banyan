package com.messagebus.scenario.client;

import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.message.model.IMessage;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 3/6/15.
 */
public class ClientUnderMultiThread {

    private static final Log logger = LogFactory.getLog(ClientUnderMultiThread.class);

    private static final String pubsuberHost = "127.0.0.1";
    private static final int    pubsuberPort = 6379;

    public static void main(String[] args) {
        logger.info("start produce...");
        ProduceClient produceClient = new ProduceClient();
        produceClient.simulate();

        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("start consume...");
        ConsumeClient consumeClient = new ConsumeClient();
        consumeClient.simulate();
    }

    private static class ProduceClient {

        private CountDownLatch counter = new CountDownLatch(2);

        public void simulate() {
            logger.info("ProduceClient simulate start");
            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMaxTotal(2);

            MessagebusSinglePool pool = new MessagebusSinglePool(pubsuberHost, pubsuberPort);

            Thread tmpThread;
            for (int i = 0; i < 2; i++) {
                tmpThread = new Thread(new ProduceThread(pool, counter));
                tmpThread.setName("produceThread-" + i);
                tmpThread.start();
            }

            try {
                counter.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            pool.destroy();
            logger.info("ProduceClient simulate end");
        }
    }

    private static class ConsumeClient {

        private CountDownLatch counter = new CountDownLatch(2);

        public void simulate() {
            logger.info("ConsumeClient simulate start");
            GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
            poolConfig.setMaxTotal(2);
            String appId = "D0fW8u2u1v7S1IvI8qoQg3dUlLL5b36q";
            MessagebusSinglePool pool = new MessagebusSinglePool(pubsuberHost, pubsuberPort);

            Thread tmpThread;
            for (int i = 0; i < 2; i++) {
                tmpThread = new Thread(new ConsumeThread(pool, counter));
                tmpThread.setName("consumeThread-" + i);
                tmpThread.start();
            }

            try {
                counter.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            pool.destroy();
            logger.info("ConsumeClient simulate end");
        }
    }

    private static class ProduceThread implements Runnable {

        private MessagebusSinglePool pool;
        private CountDownLatch       counter;

        public ProduceThread(MessagebusSinglePool pool, CountDownLatch counter) {
            this.pool = pool;
            this.counter = counter;
        }

        @Override
        public void run() {
            Messagebus client = pool.getResource();
            IMessage msg = MessageFactory.createMessage(MessageType.QueueMessage);
            msg.getMessageHeader().setContentType("text/plain");
            msg.getMessageHeader().setContentEncoding("utf-8");

            Message.MessageBody body = new Message.MessageBody();
            body.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));
            msg.setMessageBody(body);

            String secret = "kljasdoifqoikjhhhqwhebasdfasdf";
            String token = "hlkasjdhfkqlwhlfalksjdhgssssas";

            client.produce(secret, "emapDemoConsume", msg, token);
            pool.returnResource(client);

            counter.countDown();
        }
    }

    private static class ConsumeThread implements Runnable {

        private MessagebusSinglePool pool;
        private CountDownLatch       counter;

        public ConsumeThread(MessagebusSinglePool pool, CountDownLatch counter) {
            this.pool = pool;
            this.counter = counter;
        }

        @Override
        public void run() {
            String secret = "zxdjnflakwenklasjdflkqpiasdfnj";
            Messagebus client = pool.getResource();
            List<IMessage> msgs = client.consume(secret, 10);
            pool.returnResource(client);

            for (IMessage msg : msgs) {
                logger.info(msg.getMessageHeader().getMessageId());
            }

            counter.countDown();
        }
    }

}
