package com.freedom.messagebus.common.message;

import com.freedom.messagebus.common.message.messageBody.*;

public class MessageFactory {

    public static Message createMessage(MessageType messageType) {
        Message aMsg = new Message();
        aMsg.setMessageType(messageType);
        aMsg.setMessageHeader(createMessageHeader(messageType));

        switch (messageType) {
            case AppMessage:
                aMsg.setMessageBody(new AppMessageBody());
                break;

            case AuthreqMessage:
                aMsg.setMessageBody(new AuthreqMessageBody());
                break;

            case AuthrespMessage:
                aMsg.setMessageBody(new AuthrespMessageBody());
                break;

            case LookupreqMessage:
                aMsg.setMessageBody(new LookupreqMessageBody());
                break;

            case LookuprespMessage:
                aMsg.setMessageBody(new LookuprespMessageBody());
                break;

            case CacheExpiredMessage:
                aMsg.setMessageBody(new CacheExpiredMessageBody());
                break;
        }

        return aMsg;
    }

    public static IMessageHeader createMessageHeader(MessageType messageType) {
        GenericMessageHeader genericMsgHeader = new GenericMessageHeader();
        genericMsgHeader.setType(messageType.getType());
        switch (messageType) {
            case AppMessage:
                genericMsgHeader.setType("appMessage");
                break;

            case AuthreqMessage:
                genericMsgHeader.setContentType("application/json");
                genericMsgHeader.setContentEncoding("utf-8");
                break;

            case AuthrespMessage:
                genericMsgHeader.setContentType("application/json");
                genericMsgHeader.setContentEncoding("utf-8");
                genericMsgHeader.setAppId("__META__");
                break;

            case LookupreqMessage:
                genericMsgHeader.setContentType("application/json");
                genericMsgHeader.setContentEncoding("utf-8");
                break;

            case LookuprespMessage:
                genericMsgHeader.setContentType("application/json");
                genericMsgHeader.setContentEncoding("utf-8");
                genericMsgHeader.setReplyTo("__META__");
                break;

            case CacheExpiredMessage:
                genericMsgHeader.setAppId("__META__");
                genericMsgHeader.setReplyTo("__META__");
                break;
        }

        return genericMsgHeader;
    }
}
