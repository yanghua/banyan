package com.messagebus.client.message.model;


import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MessageFactory {

    private static final Log logger = LogFactory.getLog(MessageFactory.class);

    public static Message createMessage() {
        Message aMsg;

        aMsg = new Message();

        return aMsg;
    }

    public static Message createMessage(QueueingConsumer.Delivery delivery) {
        AMQP.BasicProperties properties = delivery.getProperties();
        byte[]               msgBody    = delivery.getBody();

//        String msgTypeStr = properties.getType();
//        if (msgTypeStr == null || msgTypeStr.isEmpty()) {
//            return null;
//        }

        Message msg = MessageFactory.createMessage();
        initMessage(msg, properties, msgBody);

        return msg;
    }

    public static Message createMessage(GetResponse response) {
        AMQP.BasicProperties properties = response.getProps();
        byte[]               msgBody    = response.getBody();

//      context.getChannel().basicAck(response.getEnvelope().getDeliveryTag(), false);

//        String msgTypeStr = properties.getType();

        Message msg = MessageFactory.createMessage();
        initMessage(msg, properties, msgBody);

        return msg;
    }

    private static void initMessage(Message msg,
                                    AMQP.BasicProperties properties,
                                    byte[] bodyData) {
        MessageHeaderTransfer.unbox(properties, msg);
        msg.setContent(bodyData);
    }

}
