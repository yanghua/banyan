package com.messagebus.interactor.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractInitializer {

    private static final Log logger = LogFactory.getLog(AbstractInitializer.class);

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
            logger.error("mq client inited failed with host : " + this.host, e);
            throw new RuntimeException("mq client inited failed with host : " + this.host, e);
        } catch (TimeoutException e) {
            logger.error("mq client inited failed with host : " + this.host, e);
            throw new RuntimeException("mq client inited failed with host : " + this.host, e);
        } catch (Exception e) {
            logger.error("mq client inited failed with host : " + this.host, e);
            throw new RuntimeException("mq client inited failed with host : " + this.host, e);
        }
    }

    protected void close() {
        try {
            if (this.channel != null && this.channel.isOpen()) {
                this.channel.close();
            }

            if (this.connection != null && this.connection.isOpen()) {
                this.connection.close();
            }
        } catch (IOException e) {
            logger.error("close exception ", e);
            throw new RuntimeException("close exception ", e);
        } catch (TimeoutException e) {
            logger.error("close exception ", e);
            throw new RuntimeException("close exception ", e);
        } catch (Exception e) {
            logger.error("close exception ", e);
            throw new RuntimeException("close exception ", e);
        }
    }

}
