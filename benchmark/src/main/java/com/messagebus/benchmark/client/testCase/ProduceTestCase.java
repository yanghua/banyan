package com.messagebus.benchmark.client.testCase;

import com.messagebus.benchmark.client.*;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.MessagebusUnOpenException;
import com.messagebus.client.carry.IProducer;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.common.ExceptionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProduceTestCase extends Benchmark {

    private static final Log logger = LogFactory.getLog(ProduceTestCase.class);

    private static class BasicProduce implements Runnable, ITerminater, IFetcher {

        private MessagebusSinglePool singlePool;
        private Messagebus           client;
        private Message              msg;
        private IProducer            producer;
        private boolean flag    = true;
        private long    counter = 0;

        private BasicProduce(double msgBodySize) {
            msg = TestMessageFactory.create(MessageType.QueueMessage, msgBodySize);
            singlePool = new MessagebusSinglePool(TestConfigConstant.HOST, TestConfigConstant.PORT);
            client = singlePool.getResource();
        }

        @Override
        public void run() {
            try {
                while (flag) {
//                    client.produce(, TestConfigConstant.QUEUE_NAME, msg, );
                    ++counter;
                }
            } catch (MessagebusUnOpenException e) {
                ExceptionHelper.logException(logger, e, "[BasicProduce#run]");
            } finally {
                singlePool.returnResource(client);
                singlePool.destroy();
            }
        }

        @Override
        public void terminate() {
            logger.info("closing test task ....");
            this.flag = false;
        }

        @Override
        public long fetch() {
            return this.counter;
        }
    }

    public static void main(String[] args) {
        ProduceTestCase testCase = new ProduceTestCase();

        Runnable task = new BasicProduce(TestConfigConstant.MSG_BODY_SIZE_OF_KB);

        testCase.test(task, TestConfigConstant.HOLD_TIME_OF_MILLIS,
                      TestConfigConstant.FETCH_NUM, "single_thread_produce_one_by_one_size_" +
                TestConfigConstant.MSG_BODY_SIZE_OF_KB + "_KB");
    }
}
