package com.messagebus.interactor.proxy;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.List;

public class ProxyProducer {

    public static void produce(String exchangeName,
                               Channel channel,
                               String routingKey,
                               byte[] data,
                               AMQP.BasicProperties properties
                              ) throws IOException {
        channel.basicPublish(exchangeName, routingKey, properties, data);
    }

    public static void produceWithTX(String exchangeName,
                                     Channel channel,
                                     String routingKey,
                                     byte[] data,
                                     AMQP.BasicProperties properties
                                    ) throws IOException {

        //transaction begin
        channel.txSelect();

        channel.basicPublish(exchangeName, routingKey, properties, data);

        //commit every message with wrapped a transaction
        //NOTE: it is almost for security! Not for normal, because of bad performance!!!
        channel.txCommit();
    }

    public static void batchProduce(String exchangeName,
                                    Channel channel,
                                    String routingKey,
                                    List<byte[]> dataList,
                                    AMQP.BasicProperties properties
                                   ) throws IOException {
        for (byte[] bytes : dataList)
            produce(exchangeName, channel, routingKey, bytes, properties);
    }

    public static void batchProduceWithTX(String exchangeName,
                                          Channel channel,
                                          String routingKey,
                                          List<byte[]> dataList,
                                          AMQP.BasicProperties properties
                                         ) throws IOException {
        for (byte[] bytes : dataList)
            produceWithTX(exchangeName, channel, routingKey, bytes, properties);
    }

}
