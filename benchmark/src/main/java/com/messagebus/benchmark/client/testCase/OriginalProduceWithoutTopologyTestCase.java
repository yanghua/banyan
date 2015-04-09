package com.messagebus.benchmark.client.testCase;

import com.messagebus.benchmark.client.*;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.common.Constants;
import com.messagebus.common.TestVariableInfo;
import com.messagebus.interactor.proxy.ProxyProducer;
import com.messagebus.interactor.rabbitmq.AbstractInitializer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Created by yanghua on 3/25/15.
 */
public class OriginalProduceWithoutTopologyTestCase extends Benchmark {

    private static final Log logger = LogFactory.getLog(OriginalProduceWithoutTopologyTestCase.class);

    private static class BasicProduce extends AbstractInitializer implements Runnable, ILifeCycle, IFetcher {

        private Message msg;
        private String  routingkey;
        private boolean flag    = true;
        private long    counter = 0;
        private Thread currentThread;

        private BasicProduce(String host, int msgBodySize) {
            super(host);
            msg = TestMessageFactory.create(MessageType.QueueMessage, msgBodySize);
            currentThread = new Thread(this);
            currentThread.setDaemon(true);
        }

        @Override
        public void start() {
            this.currentThread.start();
        }

        @Override
        public void terminate() {
            this.flag = false;
        }

        @Override
        public void run() {
            try {
                this.init();
                AMQP.BasicProperties header = MessageHeaderTransfer.box(msg);
                while (flag) {
                    ProxyProducer.produce(TestConfigConstant.DEFAULT_EXCHANGE_NAME_WITHOUT_TOPOLOGY,
                                          this.channel,
                                          this.getRoutingkey(),
                                          msg.getContent(),
                                          header);
                    ++counter;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    this.close();
                } catch (IOException e) {

                }
            }
        }

        @Override
        public long fetch() {
            return counter;
        }

        public String getRoutingkey() {
            return routingkey;
        }

        public void setRoutingkey(String routingkey) {
            this.routingkey = routingkey;
        }
    }

    public static void main(String[] args) {
        OriginalProduceTestCase testCase = new OriginalProduceTestCase();

        BasicProduce task = new BasicProduce(TestVariableInfo.RABBITMQ_SERVER_HOST, TestConfigConstant.MSG_BODY_SIZE_OF_BYTE);
        task.setRoutingkey("");

        testCase.test(task, TestConfigConstant.HOLD_TIME_OF_MILLIS,
                      TestConfigConstant.FETCH_NUM, "one_thread_original_produce_without_topology_one_by_one_" +
                TestConfigConstant.MSG_BODY_SIZE_OF_BYTE + "_Byte");
    }

}
