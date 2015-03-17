package com.messagebus.client.message.transfer;

import com.messagebus.client.message.model.IMessageHeader;
import com.messagebus.client.message.model.MessageType;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MessageHeaderTransfer {

    private static final Log logger = LogFactory.getLog(MessageHeaderTransfer.class);

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

    public static IMessageHeader unbox(AMQP.BasicProperties properties,
                                       MessageType msgType,
                                       IMessageHeader msgHeader) {
        //common properties
        msgHeader.setCorrelationId(properties.getCorrelationId());
        msgHeader.setHeaders(properties.getHeaders());
        msgHeader.setTimestamp(properties.getTimestamp());
        String msgIdStr = properties.getMessageId();
        if (msgIdStr != null && !msgIdStr.isEmpty())
            msgHeader.setMessageId(Long.parseLong(msgIdStr));
        else
            logger.error("[unbox] illegal message id (can not be null) ");

        msgHeader.setContentEncoding(properties.getContentEncoding());
        msgHeader.setContentType(properties.getContentType());
        msgHeader.setAppId(properties.getAppId());
        msgHeader.setReplyTo(properties.getReplyTo());

        return msgHeader;
    }

}
