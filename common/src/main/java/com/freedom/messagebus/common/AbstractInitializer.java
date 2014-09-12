package com.freedom.messagebus.common;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

/**
 * User: yanghua
 * Date: 7/2/14
 * Time: 3:16 PM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public abstract class AbstractInitializer {

    protected ConnectionFactory connectionFactory;
    protected Connection        connection;
    protected Channel           channel;
    private   String            host;

    protected AbstractInitializer(String host) {
        this.host = host;
    }

    protected void init() throws IOException {
        this.connectionFactory = new ConnectionFactory();
        this.connectionFactory.setHost(this.host);
        this.connection = this.connectionFactory.newConnection();
        this.channel = this.connection.createChannel();
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
