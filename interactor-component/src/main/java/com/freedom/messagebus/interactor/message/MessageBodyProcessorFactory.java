package com.freedom.messagebus.interactor.message;


import com.freedom.messagebus.common.message.MessageType;
import com.freedom.messagebus.interactor.message.bodyprocessor.AppMsgBodyProcessor;

public class MessageBodyProcessorFactory {

    public static IMessageBodyProcessor createMsgBodyProcessor(MessageType messageType) {
        IMessageBodyProcessor processor = null;
        switch (messageType) {
            case AppMessage:
                processor = new AppMsgBodyProcessor();
                break;

            case AuthreqMessage:

                break;

            case AuthrespMessage:

                break;

            case LookupreqMessage:

                break;

            case LookuprespMessage:

                break;

            case CacheExpiredMessage:

                break;
        }

        return processor;
    }

}
