package com.freedom.messagebus.scenario.client;

import com.freedom.messagebus.client.Messagebus;
import com.freedom.messagebus.client.MessagebusConnectedFailedException;
import com.freedom.messagebus.client.MessagebusUnOpenException;
import com.freedom.messagebus.common.message.BroadcastMessage;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.common.message.MessageFactory;
import com.freedom.messagebus.common.message.MessageType;

public class BroadcastTemplate {

    private static final String appId = "LAJFOWFALSKDJFALLKAJSDFLKSDFJLWKJ";
    private static final String host  = "115.29.96.85";
    private static final int    port  = 2181;

    public static void broadcast() {
        String queueName = "crm";
        Message msg = MessageFactory.createMessage(MessageType.BroadcastMessage);
        msg.getMessageHeader().setReplyTo(queueName);
        msg.getMessageHeader().setContentType("text/plain");
        msg.getMessageHeader().setContentEncoding("utf-8");

        BroadcastMessage.BroadcastMessageBody body = new BroadcastMessage.BroadcastMessageBody();
        body.setContent("test".getBytes());
        msg.setMessageBody(body);

        Messagebus client = Messagebus.getInstance(appId);
        client.setZkHost(host);
        client.setZkPort(port);

        try {
            client.open();
            client.getBroadcaster().broadcast(new Message[]{msg});
        } catch (MessagebusConnectedFailedException | MessagebusUnOpenException e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }

    public static void main(String[] args) {
        broadcast();
    }

}
