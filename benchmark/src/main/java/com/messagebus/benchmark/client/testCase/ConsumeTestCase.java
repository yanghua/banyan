package com.messagebus.benchmark.client.testCase;

import com.messagebus.benchmark.client.Benchmark;
import com.messagebus.benchmark.client.IFetcher;
import com.messagebus.benchmark.client.ITerminater;
import com.messagebus.benchmark.client.TestConfigConstant;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.MessagebusUnOpenException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConsumeTestCase extends Benchmark {

    private static final Log logger = LogFactory.getLog(ConsumeTestCase.class);

    private static class BasicConsume implements Runnable, ITerminater, IFetcher {

        private MessagebusSinglePool singlePool;
        private Messagebus           client;
        private long counter = 0;

        private BasicConsume() {
            singlePool = new MessagebusSinglePool(TestConfigConstant.HOST, TestConfigConstant.PORT);
            client = singlePool.getResource();
        }

        @Override
        public long fetch() {
            return this.counter;
        }

        @Override
        public void terminate() {
            logger.info("closing test task ....");
        }

        @Override
        public void run() {
            try {

//                client.consume(,
//                    Integer.MAX_VALUE, TimeUnit.SECONDS, new IMessageReceiveListener() {
//                        @Override
//                        public void onMessage(Message message) {
//                            ++counter;
//                        }
//                    });
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
