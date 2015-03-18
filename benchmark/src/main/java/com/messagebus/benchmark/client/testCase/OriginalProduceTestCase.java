package com.messagebus.benchmark.client.testCase;

import com.messagebus.benchmark.client.*;
import com.messagebus.client.message.model.IMessage;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.client.message.transfer.MsgBodyTransfer;
import com.messagebus.common.Constants;
import com.messagebus.interactor.proxy.ProxyProducer;
import com.messagebus.interactor.rabbitmq.AbstractInitializer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class OriginalProduceTestCase extends Benchmark {

    private static final Log logger = LogFactory.getLog(OriginalProduceTestCase.class);

    private static class BasicProduce extends AbstractInitializer implements Runnable, ITerminater, IFetcher {

        private IMessage msg;
        private String   routingkey;
        private boolean flag    = true;
        private long    counter = 0;

        private BasicProduce(String host, double msgBodySize) {
            super(host);
            msg = TestMessageFactory.create(MessageType.QueueMessage, msgBodySize);
        }

        @Override
        public void terminate() {
            this.flag = false;
        }

        @Override
        public void run() {
            try {
                this.init();
                byte[] msgBodyOfBytes = MsgBodyTransfer.box(msg.getMessageBody());
                AMQP.BasicProperties header = MessageHeaderTransfer.box(msg.getMessageHeader());
                while (flag) {
                    ProxyProducer.produce(Constants.PROXY_EXCHANGE_NAME,
                                          this.channel,
                                          this.getRoutingkey(),
                                          msgBodyOfBytes,
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

        String host = "172.16.206.30";

        BasicProduce task = new BasicProduce(host, TestConfigConstant.MSG_BODY_SIZE_OF_KB);
        task.setRoutingkey("routingkey.proxy.message.business.crm");

        testCase.test(task, TestConfigConstant.HOLD_TIME_OF_MILLIS,
                      TestConfigConstant.FETCH_NUM, "single_thread_original_produce_one_by_one_" +
                TestConfigConstant.MSG_BODY_SIZE_OF_KB + "_KB");
    }


}
