package com.messagebus.interactor.rabbitmq;

import com.google.common.base.Strings;
import com.rabbitmq.client.Address;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractInitializer {

    private static final Log logger = LogFactory.getLog(AbstractInitializer.class);

    protected Connection connection;
    protected Channel    channel;
    protected String     mqConnectionStr;

    protected AbstractInitializer(String mqConnectionStr) {
        this.mqConnectionStr = mqConnectionStr;
    }

    protected void init() {
        if (Strings.isNullOrEmpty(mqConnectionStr)) {
            logger.info("can not get mq connection info");
            throw new RuntimeException("can not get mq connection info");
        }
        String[] hostPortPairArr = mqConnectionStr.split(",");

        Address[] addresses = new com.rabbitmq.client.Address[hostPortPairArr.length];

        for (int i = 0; i < hostPortPairArr.length; i++) {
            String[] hostPortArr = hostPortPairArr[i].split(":");
            com.rabbitmq.client.Address address = new com.rabbitmq.client.Address(
                    hostPortArr[0], Integer.parseInt(hostPortArr[1])
            );
            addresses[i] = address;
        }

        ConnectionFactory connectionFactory = new ConnectionFactory();

        connectionFactory.setAutomaticRecoveryEnabled(true);
        connectionFactory.setTopologyRecoveryEnabled(true);
        connectionFactory.setConnectionTimeout(60000);
        connectionFactory.setRequestedHeartbeat(10);

        try {
            this.connection = connectionFactory.newConnection(addresses);
            this.channel = this.connection.createChannel();
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            logger.error(e);
            throw new RuntimeException(e);
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
