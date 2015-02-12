package com.freedom.messagebus.interactor.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

public abstract class AbstractInitializer {

    protected ConnectionFactory connectionFactory;
    protected Connection        connection;
    protected Channel           channel;
    protected String            host;

    protected AbstractInitializer(String host) {
        this.host = host;
    }

    protected void init() {
        try {
            this.connectionFactory = new ConnectionFactory();
            this.connectionFactory.setHost(this.host);
            this.connection = this.connectionFactory.newConnection();
            this.channel = this.connection.createChannel();
        } catch (IOException e) {
            throw new RuntimeException("mq client inited failed :  " + e.toString());
        }
    }

    protected void close() throws IOException {
        if (this.channel != null && this.channel.isOpen()) {
            this.channel.close();
        }

        if (this.connection != null && this.connection.isOpen()) {
            this.connection.close();
        }
    }

}
