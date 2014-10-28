package com.freedom.messagebus.benchmark.client.testCase;

import com.freedom.messagebus.benchmark.client.Benchmark;
import com.freedom.messagebus.benchmark.client.IFetcher;
import com.freedom.messagebus.benchmark.client.ITerminater;
import com.freedom.messagebus.benchmark.client.TestConfigConstant;
import com.freedom.messagebus.client.*;
import com.freedom.messagebus.common.message.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class ConsumeTestCase extends Benchmark {

    private static final Log logger = LogFactory.getLog(ConsumeTestCase.class);

    private static class BasicConsume implements Runnable, ITerminater, IFetcher {

        private Messagebus client;
        private IConsumer  consumer;
        private final Object lockObj = new Object();
        private       long   counter = 0;

        private BasicConsume() {
            client = Messagebus.getInstance(TestConfigConstant.APP_KEY);
            client.setZkHost(TestConfigConstant.HOST);
            client.setZkPort(TestConfigConstant.PORT);
        }

        @Override
        public long fetch() {
            return this.counter;
        }

        @Override
        public void terminate() {
            logger.info("closing test task ....");
            synchronized (lockObj) {
                lockObj.notifyAll();
            }
        }

        @Override
        public void run() {
            IReceiverCloser closer = null;
            try {
                client.open();
                consumer = client.getConsumer();

                synchronized (lockObj) {
                    closer = consumer.consume(TestConfigConstant.QUEUE_NAME, new IMessageReceiveListener() {
                        @Override
                        public void onMessage(Message message, IReceiverCloser consumerCloser) {
                            ++counter;
                        }
                    });

                    lockObj.wait(0);
                }
            } catch (MessagebusConnectedFailedException | MessagebusUnOpenException |
                IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (closer != null)
                    closer.close();
                if (client != null)
                    client.close();
            }
        }
    }

    public static void main(String[] args) {
//        produce some message for consuming
//        TestUtility.produce(50_0000);
//
//        try {
//            TimeUnit.SECONDS.sleep(30);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        ConsumeTestCase testCase = new ConsumeTestCase();

        Runnable task = new BasicConsume();
        testCase.test(task, TestConfigConstant.HOLD_TIME_OF_MILLIS, TestConfigConstant.FETCH_NUM,
                      "single_thread_consume_async_size_" + TestConfigConstant.MSG_BODY_SIZE_OF_KB + "_KB");
    }

}
