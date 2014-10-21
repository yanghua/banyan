package com.freedom.messagebus.scenario.client;

import com.freedom.messagebus.client.Messagebus;
import com.freedom.messagebus.client.MessagebusConnectedFailedException;
import com.freedom.messagebus.client.MessagebusUnOpenException;
import com.freedom.messagebus.common.message.*;

public class PublishTemplate {

    private static final String appkey = "LAJFOWFALSKDJFALLKAJSDFLKSDFJLWKJ";
    private static final String host   = "115.29.96.85";
    private static final int    port   = 2181;

    public static void publish() {
        Message msg = MessageFactory.createMessage(MessageType.PubSubMessage);
        msg.getMessageHeader().setReplyTo("crm");
        msg.getMessageHeader().setContentType("text/plain");
        msg.getMessageHeader().setContentEncoding("utf-8");

        PubSubMessage.PubSubMessageBody body = new PubSubMessage.PubSubMessageBody();
        body.setContent("test".getBytes());
        msg.setMessageBody(body);

        Messagebus client = Messagebus.getInstance(appkey);
        client.setZkHost(host);
        client.setZkPort(port);

        try {
            client.open();
            client.getPublisher().publish(new Message[] {msg});
        } catch (MessagebusConnectedFailedException | MessagebusUnOpenException e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }

    public static void main(String[] args) {
        publish();
    }

}
