package com.messagebus.benchmark.client.testCase;

import com.messagebus.benchmark.client.*;
import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.MessagebusUnOpenException;
import com.messagebus.client.message.model.Message;
import com.messagebus.common.TestVariableInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;

public class ConsumeTestCase extends Benchmark {

    private static final Log logger = LogFactory.getLog(ConsumeTestCase.class);

    private static class BasicConsume implements Runnable, ILifeCycle, IFetcher {

        private MessagebusSinglePool singlePool;
        private Messagebus           client;
        private long counter = 0;
        private Thread currentThread;

        private BasicConsume() {
            singlePool = new MessagebusSinglePool(TestVariableInfo.PUBSUBER_HOST, TestVariableInfo.PUBSUBER_PORT);
            client = singlePool.getResource();
            currentThread = new Thread(this);
            currentThread.setDaemon(true);
        }

        @Override
        public long fetch() {
            return this.counter;
        }

        @Override
        public void start() {
            this.currentThread.start();
        }

        @Override
        public void terminate() {
            logger.info("closing test task ....");
            this.currentThread.interrupt();
        }

        @Override
        public void run() {
            try {
                client.consume(TestConfigConstant.CONSUMER_SECRET,
                               Integer.MAX_VALUE, TimeUnit.SECONDS, new IMessageReceiveListener() {
                        @Override
                        public void onMessage(Message message) {
                            ++counter;
                        }
                    });
            } catch (MessagebusUnOpenException e) {
                e.printStackTrace();
            } finally {
                singlePool.returnResource(client);
                singlePool.destroy();
            }
        }
    }

    public static void main(String[] args) {
//        produce some message for consuming
        TestUtility.produce(500000);

        try {
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ConsumeTestCase testCase = new ConsumeTestCase();

        Runnable task = new BasicConsume();
        testCase.test(task, TestConfigConstant.HOLD_TIME_OF_MILLIS, TestConfigConstant.FETCH_NUM,
                      "single_thread_consume_async_" + TestConfigConstant.MSG_BODY_SIZE_OF_BYTE + "_KB");
    }

}
