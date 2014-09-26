package com.freedom.messagebus.interactor.proxy;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class ProxyProducer {

    public static void produce(@NotNull String exchangeName,
                               @NotNull Channel channel,
                               @NotNull String routingKey,
                               @NotNull byte[] data,
                               @NotNull AMQP.BasicProperties properties
                              ) throws IOException {
        channel.basicPublish(exchangeName, routingKey, properties, data);
    }

    public static void produceWithTX(@NotNull String exchangeName,
                                     @NotNull Channel channel,
                                     @NotNull String routingKey,
                                     @NotNull byte[] data,
                                     @NotNull AMQP.BasicProperties properties
                                    ) throws IOException {

        //transaction begin
        channel.txSelect();

        channel.basicPublish(exchangeName, routingKey, properties, data);

        //commit every message with wrapped a transaction
        //NOTE: it is almost for security! Not for normal, because of bad performance!!!
        channel.txCommit();
    }

    public static void batchProduce(@NotNull String exchangeName,
                                    @NotNull Channel channel,
                                    @NotNull String routingKey,
                                    @NotNull List<byte[]> dataList,
                                    @NotNull AMQP.BasicProperties properties
                                   ) throws IOException {
        for (byte[] bytes : dataList)
            produce(exchangeName, channel, routingKey, bytes, properties);
    }

    public static void batchProduceWithTX(@NotNull String exchangeName,
                                          @NotNull Channel channel,
                                          @NotNull String routingKey,
                                          @NotNull List<byte[]> dataList,
                                          @NotNull AMQP.BasicProperties properties
                                         ) throws IOException {
        for (byte[] bytes : dataList)
            produceWithTX(exchangeName, channel, routingKey, bytes, properties);
    }

}
