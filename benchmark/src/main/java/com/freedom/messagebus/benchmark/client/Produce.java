package com.freedom.messagebus.benchmark.client;

import com.freedom.messagebus.client.Messagebus;
import com.freedom.messagebus.client.MessagebusConnectedFailedException;
import com.freedom.messagebus.client.MessagebusUnOpenException;
import com.freedom.messagebus.common.message.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Produce {

    private static final Log logger = LogFactory.getLog(Produce.class);

    private static final String appkey    = "LAJFOWFALSKDJFALLKAJSDFLKSDFJLWKJ";
    private static final String host      = "115.29.96.85";
    private static final int    port      = 2181;
    private static final String queueName = "crm";

    public static void main(String[] args) {


    }

    private static class BasicProduce implements Runnable {

        private Messagebus client;
        private Message    msg;

        private BasicProduce() {
            msg = MessageFactory.createMessage(MessageType.QueueMessage);

            QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
            body.setContent("test".getBytes());
            msg.setMessageBody(body);

            client = Messagebus.getInstance(appkey);
            client.setZkHost(host);
            client.setZkPort(port);
        }

        @Override
        public void run() {
            try {
                client.open();
                client.getProducer().produce(msg, queueName);
            } catch (MessagebusConnectedFailedException | MessagebusUnOpenException e) {
//                logger
            } finally {
                client.close();
            }
        }
    }

}
