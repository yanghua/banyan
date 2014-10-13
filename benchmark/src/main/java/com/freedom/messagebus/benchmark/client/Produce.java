package com.freedom.messagebus.benchmark.client;

import com.freedom.messagebus.client.Messagebus;
import com.freedom.messagebus.client.MessagebusConnectedFailedException;
import com.freedom.messagebus.client.MessagebusUnOpenException;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.common.message.MessageFactory;
import com.freedom.messagebus.common.message.MessageType;
import com.freedom.messagebus.common.message.messageBody.AppMessageBody;
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
            msg = MessageFactory.createMessage(MessageType.AppMessage);

            AppMessageBody appMessageBody = (AppMessageBody) msg.getMessageBody();
            appMessageBody.setMessageBody("test".getBytes());

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
                logger
            } finally {
                client.close();
            }
        }
    }

}
