package com.freedom.messagebus.common.message;


public class MessageFactory {

    public static Message createMessage(MessageType messageType) {
        Message amessage = new Message();
        switch (messageType) {
            case AppMessage:

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

        return amessage;
    }

}
