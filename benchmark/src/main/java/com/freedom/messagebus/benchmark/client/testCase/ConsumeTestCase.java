package com.freedom.messagebus.benchmark.client.testCase;

import com.freedom.messagebus.benchmark.client.Benchmark;
import com.freedom.messagebus.benchmark.client.IFetcher;
import com.freedom.messagebus.benchmark.client.ITerminater;
import com.freedom.messagebus.benchmark.client.TestConfigConstant;
import com.freedom.messagebus.client.*;
import com.freedom.messagebus.client.message.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;

public class ConsumeTestCase extends Benchmark {

    private static final Log logger = LogFactory.getLog(ConsumeTestCase.class);

    private static class BasicConsume implements Runnable, ITerminater, IFetcher {

        private Messagebus    client;
        private       long   counter = 0;

        private BasicConsume() {
            client = new Messagebus(TestConfigConstant.APP_KEY);
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
        }

        @Override
        public void run() {
            try {
                client.open();

                client.consume(
                    new IMessageReceiveListener() {
                        @Override
                        public void onMessage(Message message) {
                            ++counter;
                        }
                    }, Integer.MAX_VALUE, TimeUnit.SECONDS);
            } catch (MessagebusConnectedFailedException | MessagebusUnOpenException e) {
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
