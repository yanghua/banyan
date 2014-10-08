package com.freedom.messagebus.scenario.client;

import com.freedom.messagebus.client.Messagebus;
import com.freedom.messagebus.client.MessagebusConnectedFailedException;
import com.freedom.messagebus.client.MessagebusUnOpenException;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.common.message.MessageFactory;
import com.freedom.messagebus.common.message.MessageType;
import com.freedom.messagebus.common.message.messageBody.AppMessageBody;

public class ProduceTemplate {

    private static final String appkey = "LAJFOWFALSKDJFALLKAJSDFLKSDFJLWKJ";
    private static final String host   = "115.29.96.85";
    private static final int    port   = 2181;

    /**
     * produce的常见场景有如下几个特点：
     * (1)按需使用
     * (2)生命周期短
     * (3)如果发生的消息量大，可使用多线程发送
     */
    public static void produce() {
        Message msg = MessageFactory.createMessage(MessageType.AppMessage);
        String queueName = "crm";

        AppMessageBody appMessageBody = (AppMessageBody) msg.getMessageBody();
        appMessageBody.setMessageBody("test".getBytes());

        Messagebus client = Messagebus.getInstance(appkey);
        client.setZkHost(host);
        client.setZkPort(port);

        try {
            client.open();
            client.getProducer().produce(msg, queueName);
        } catch (MessagebusConnectedFailedException | MessagebusUnOpenException e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }

    public static void main(String[] args) {
        produce();
    }

}
