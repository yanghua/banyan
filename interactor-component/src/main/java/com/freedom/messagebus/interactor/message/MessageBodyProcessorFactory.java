package com.freedom.messagebus.interactor.message;


import com.freedom.messagebus.common.message.MessageType;
import com.freedom.messagebus.interactor.message.bodyprocessor.BroadcastMsgBodyProcessor;
import com.freedom.messagebus.interactor.message.bodyprocessor.PubSubMsgBodyProcessor;
import com.freedom.messagebus.interactor.message.bodyprocessor.QueueMsgBodyProcessor;

public class MessageBodyProcessorFactory {

    public static IMessageBodyProcessor createMsgBodyProcessor(MessageType messageType) {
        IMessageBodyProcessor processor = null;
        switch (messageType) {
            case QueueMessage:
                processor = new QueueMsgBodyProcessor();
                break;

            case AuthreqMessage:

                break;

            case AuthrespMessage:

                break;

            case PubSubMessage:
                processor = new PubSubMsgBodyProcessor();
                break;

            case BroadcastMessage:
                processor = new BroadcastMsgBodyProcessor();
                break;

        }

        return processor;
    }

}
