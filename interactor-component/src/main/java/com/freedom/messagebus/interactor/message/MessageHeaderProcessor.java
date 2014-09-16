package com.freedom.messagebus.interactor.message;

import com.freedom.messagebus.common.message.IMessageHeader;
import com.freedom.messagebus.common.message.MessageFactory;
import com.freedom.messagebus.common.message.MessageType;
import com.rabbitmq.client.AMQP;
import org.jetbrains.annotations.NotNull;

public class MessageHeaderProcessor {

    public static AMQP.BasicProperties box(IMessageHeader header) {
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        return builder.messageId(String.valueOf(header.getMessageId()))
               .appId(header.getAppId())
               .clusterId(header.getClusterId())
               .contentEncoding(header.getContentEncoding())
               .contentType(header.getContentType())
               .correlationId(header.getCorrelationId())
               .deliveryMode((int) header.getDeliveryMode())
               .expiration(header.getExpiration())
               .headers(header.getHeaders())
               .priority((int) header.getPriority())
               .replyTo(header.getReplyTo())
               .timestamp(header.getTimestamp())
               .type(header.getType())
               .userId(header.getUserId())
               .build();
    }

    public static IMessageHeader unbox(@NotNull AMQP.BasicProperties properties, MessageType msgType) {
        IMessageHeader msgHeader = MessageFactory.createMessageHeader(msgType);

        //common properties
        msgHeader.setCorrelationId(properties.getCorrelationId());
        msgHeader.setHeaders(properties.getHeaders());
        msgHeader.setTimestamp(properties.getTimestamp());
        msgHeader.setMessageId(properties.getMessageId());

        switch (msgType) {
            case AppMessage:
                msgHeader.setContentEncoding(properties.getContentEncoding());
                msgHeader.setContentType(properties.getContentType());
                msgHeader.setAppId(properties.getAppId());
                msgHeader.setReplyTo(properties.getReplyTo());
                break;

            case AuthreqMessage:
                msgHeader.setAppId(properties.getAppId());
                msgHeader.setReplyTo(properties.getReplyTo());
                break;

            case AuthrespMessage:
                msgHeader.setReplyTo(properties.getReplyTo());
                break;

            case LookupreqMessage:
                msgHeader.setAppId(properties.getAppId());
                msgHeader.setReplyTo(properties.getReplyTo());
                break;

            case LookuprespMessage:
                msgHeader.setAppId(properties.getAppId());
                break;

            case CacheExpiredMessage:
                msgHeader.setContentEncoding(properties.getContentEncoding());
                msgHeader.setContentType(properties.getContentType());
                break;
        }

        return null;
    }

}
