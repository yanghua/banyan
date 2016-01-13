package com.messagebus.interactor.proxy;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class ProxyConsumer {

    private static final Log logger = LogFactory.getLog(ProxyConsumer.class);

    public static QueueingConsumer consume(Channel channel,
                                           String queueName,
                                           boolean autoAck,
                                           String consumerTag) throws IOException {
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, autoAck, consumerTag, consumer);
        return consumer;
    }


    public static GetResponse consumeSingleMessage(Channel channel,
                                                   String queueName) throws IOException {
        return channel.basicGet(queueName, true);
    }

}
