package com.messagebus.client.message.model;


public class MessageFactory {

    public static Message createMessage(MessageType messageType) {
        Message aMsg;

        switch (messageType) {
            case QueueMessage:
                aMsg = new QueueMessage();
                aMsg.setMessageBody(new QueueMessage.QueueMessageBody());
                break;

            case AuthreqMessage:
                aMsg = new PubSubMessage();
                break;

            case AuthrespMessage:
                aMsg = new PubSubMessage();
                break;

            case PubSubMessage:
                aMsg = new PubSubMessage();
                aMsg.setMessageBody(new PubSubMessage.PubSubMessageBody());
                break;

            case BroadcastMessage:
                aMsg = new BroadcastMessage();
                aMsg.setMessageBody(new BroadcastMessage.BroadcastMessageBody());
                break;

            default:
                aMsg = new QueueMessage();
                break;
        }

        return aMsg;
    }

}
