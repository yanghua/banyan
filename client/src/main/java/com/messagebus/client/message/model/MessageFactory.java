package com.messagebus.client.message.model;


public class MessageFactory {

    public static IMessage createMessage(MessageType messageType) {
        IMessage aMsg;

        aMsg = new Message();
        aMsg.getMessageHeader().setType(messageType.getType());
        aMsg.setMessageBody(new Message.MessageBody());

        return aMsg;
    }

}
