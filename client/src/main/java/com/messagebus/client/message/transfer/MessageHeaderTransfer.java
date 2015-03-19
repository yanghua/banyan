package com.messagebus.client.message.transfer;

import com.messagebus.client.message.model.Message;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MessageHeaderTransfer {

    private static final Log logger = LogFactory.getLog(MessageHeaderTransfer.class);

    public static AMQP.BasicProperties box(Message msg) {
        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();

        return builder.messageId(String.valueOf(msg.getMessageId()))
                      .appId(msg.getAppId())
                      .clusterId(msg.getClusterId())
                      .contentEncoding(msg.getContentEncoding())
                      .contentType(msg.getContentType())
                      .correlationId(msg.getCorrelationId())
                      .deliveryMode((int) msg.getDeliveryMode())
                      .expiration(msg.getExpiration())
                      .headers(msg.getHeaders())
                      .priority((int) msg.getPriority())
                      .replyTo(msg.getReplyTo())
                      .timestamp(msg.getTimestamp())
                      .type(msg.getType())
                      .userId(msg.getUserId())
                      .build();
    }

    public static Message unbox(AMQP.BasicProperties properties,
                                Message msg) {
        //common properties
        msg.setCorrelationId(properties.getCorrelationId());
        msg.setHeaders(properties.getHeaders());
        msg.setTimestamp(properties.getTimestamp());
        String msgIdStr = properties.getMessageId();
        if (msgIdStr != null && !msgIdStr.isEmpty())
            msg.setMessageId(Long.parseLong(msgIdStr));
        else
            logger.error("[unbox] illegal message id (can not be null) ");

        msg.setContentEncoding(properties.getContentEncoding());
        msg.setContentType(properties.getContentType());
        msg.setAppId(properties.getAppId());
        msg.setReplyTo(properties.getReplyTo());

        return msg;
    }

}
