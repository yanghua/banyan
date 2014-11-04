package com.freedom.messagebus.interactor.proxy;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ProxyConsumer {

    private static final Log logger = LogFactory.getLog(ProxyConsumer.class);

    /**
     * common consume mostly for customization
     *
     * @param channel
     * @param queueName
     * @param consumerTag
     * @return
     * @throws IOException
     */
    public static QueueingConsumer consume(@NotNull Channel channel,
                                           @NotNull String queueName,
                                           String consumerTag) throws IOException {
        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, false, consumerTag, consumer);
        return consumer;
    }

    public static GetResponse consumeSingleMessage(@NotNull Channel channel,
                                                   @NotNull String queueName) throws IOException {
        boolean notAutoAck = false;
        return channel.basicGet(queueName, notAutoAck);
    }

}
