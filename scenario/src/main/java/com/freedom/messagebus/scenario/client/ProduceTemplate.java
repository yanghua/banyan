package com.freedom.messagebus.scenario.client;

import com.freedom.messagebus.client.Messagebus;
import com.freedom.messagebus.client.MessagebusConnectedFailedException;
import com.freedom.messagebus.client.MessagebusUnOpenException;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.model.MessageFactory;
import com.freedom.messagebus.client.message.model.MessageType;
import com.freedom.messagebus.client.message.model.QueueMessage;
import com.freedom.messagebus.common.Constants;

public class ProduceTemplate {

    private static final String appId = "djB5l1n7PbFsszF5817JOon2895El1KP";     //ucp
    private static final String host  = "127.0.0.1";
    private static final int    port  = 6379;

    /**
     * produce的常见场景有如下几个特点：
     * (1)按需使用
     * (2)生命周期短
     * (3)如果发生的消息量大，可使用多线程发送
     */
    public static void produce() {
        String queueName = "erp";

        Message msg = MessageFactory.createMessage(MessageType.QueueMessage);
        msg.getMessageHeader().setReplyTo(queueName);
        msg.getMessageHeader().setContentType("text/plain");
        msg.getMessageHeader().setContentEncoding("utf-8");

        QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
        body.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));

        msg.setMessageBody(body);

        Messagebus client = Messagebus.createClient(appId);
        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

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
