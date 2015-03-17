package com.messagebus.scenario.client;

import com.messagebus.client.MessagebusPool;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

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
            TimeUnit.SECONDS.sleep(10);
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
            String appId = "djB5l1n7PbFsszF5817JOon2895El1KP";
            MessagebusPool pool = new MessagebusPool(poolConfig, appId, pubsuberHost, pubsuberPort);

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
            MessagebusPool pool = new MessagebusPool(poolConfig, appId, pubsuberHost, pubsuberPort);

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

        private MessagebusPool pool;
        private CountDownLatch counter;

        public ProduceThread(MessagebusPool pool, CountDownLatch counter) {
            this.pool = pool;
            this.counter = counter;
        }

        @Override
        public void run() {
//            Messagebus client = pool.getResource();
//            Message msg = MessageFactory.createMessage(MessageType.QueueMessage);
//            msg.getMessageHeader().setContentType("text/plain");
//            msg.getMessageHeader().setContentEncoding("utf-8");
//
//            QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
//            body.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));
//            msg.setMessageBody(body);
//
//            client.produce(, "erp", msg, );
//            pool.returnResource(client);
//
//            counter.countDown();
        }
    }

    private static class ConsumeThread implements Runnable {

        private MessagebusPool pool;
        private CountDownLatch counter;

        public ConsumeThread(MessagebusPool pool, CountDownLatch counter) {
            this.pool = pool;
            this.counter = counter;
        }

        @Override
        public void run() {
//            Messagebus client = pool.getResource();
//            List<Message> msgs = client.consume(, 10);
//            pool.returnResource(client);
//
//            for (Message msg : msgs) {
//                logger.info(msg.getMessageHeader().getMessageId());
//            }
//
//            counter.countDown();
        }
    }

}
