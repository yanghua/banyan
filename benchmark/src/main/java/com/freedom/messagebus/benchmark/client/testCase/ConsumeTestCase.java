package com.freedom.messagebus.benchmark.client.testCase;

import com.freedom.messagebus.benchmark.client.Benchmark;
import com.freedom.messagebus.benchmark.client.IFetcher;
import com.freedom.messagebus.benchmark.client.ITerminater;
import com.freedom.messagebus.benchmark.client.TestConfigConstant;
import com.freedom.messagebus.client.*;
import com.freedom.messagebus.client.impl.AsyncConsumer;
import com.freedom.messagebus.client.message.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConsumeTestCase extends Benchmark {

    private static final Log logger = LogFactory.getLog(ConsumeTestCase.class);

    private static class BasicConsume implements Runnable, ITerminater, IFetcher {

        private Messagebus    client;
        private AsyncConsumer asyncConsumer;
        private final Object lockObj = new Object();
        private       long   counter = 0;

        private BasicConsume() {
            client = Messagebus.createClient(TestConfigConstant.APP_KEY);
            client.setPubsuberHost(TestConfigConstant.HOST);
            client.setPubsuberPort(TestConfigConstant.PORT);
        }

        @Override
        public long fetch() {
            return this.counter;
        }

        @Override
        public void terminate() {
            logger.info("closing test task ....");
            if (asyncConsumer != null) {
                asyncConsumer.shutdown();
            }
        }

        @Override
        public void run() {
            try {
                client.open();

                synchronized (lockObj) {
                    asyncConsumer = client.getAsyncConsumer(
                        TestConfigConstant.QUEUE_NAME,
                        new IMessageReceiveListener() {
                            @Override
                            public void onMessage(Message message, IReceiverCloser consumerCloser) {
                                ++counter;
                            }
                        });

                    lockObj.wait(0);
                }
            } catch (MessagebusConnectedFailedException | MessagebusUnOpenException | InterruptedException e) {
                e.printStackTrace();
            } finally {
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

//        ConsumeTestCase testCase = new ConsumeTestCase();
//
//        Runnable task = new BasicConsume();
//        testCase.test(task, TestConfigConstant.HOLD_TIME_OF_MILLIS, TestConfigConstant.FETCH_NUM,
//                      "single_thread_consume_async_" + TestConfigConstant.MSG_BODY_SIZE_OF_KB + "_KB");
    }

}
