package com.messagebus.client.message.model;


public class MessageFactory {

    public static Message createMessage(MessageType messageType) {
        Message aMsg;

        aMsg = new Message();
        aMsg.setType(messageType.getType());

        return aMsg;
    }

}
