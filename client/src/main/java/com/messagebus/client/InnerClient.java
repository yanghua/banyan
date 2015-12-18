package com.messagebus.client;

import com.google.common.eventbus.EventBus;
import com.messagebus.client.event.component.ClientDestroyEvent;
import com.messagebus.client.event.component.ClientInitedEvent;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by yanghua on 3/1/15.
 */
abstract class InnerClient {

    private static final Log logger = LogFactory.getLog(Messagebus.class);

    private   ConfigManager configManager;
    private   Connection    connection;
    protected EventBus      componentEventBus;
    protected EventBus      carryEventBus;

    private   Channel        channel;
    protected GenericContext context;

    private AtomicBoolean isOpen = new AtomicBoolean(false);

    public InnerClient() {
        context = new GenericContext();
    }

    private void open() {
        if (this.isOpen())
            return;

        try {
            this.channel = this.connection.createChannel();
            context.setChannel(this.channel);
        } catch (IOException e) {
            logger.error("create channel error, connection host : " + this.connection.getAddress().getHostAddress()
                    + " connection port : " + this.connection.getPort(), e);
            throw new RuntimeException(e);
        }

        carryEventBus = new EventBus("carryEventBus");

        context.setCarryEventBus(carryEventBus);
        context.setConfigManager(this.configManager);
        context.setConnection(this.connection);

        this.isOpen.compareAndSet(false, true);

        this.componentEventBus.post(new ClientInitedEvent());
    }

    private void close() {
        //release all resource
        synchronized (this.channel) {
            try {
                if (this.channel != null && this.channel.isOpen())
                    this.channel.close();

                this.carryEventBus = null;

                this.isOpen.compareAndSet(true, false);

                this.componentEventBus.post(new ClientDestroyEvent());
            } catch (IOException e) {
                logger.error("close inner client exception : ", e);
                throw new RuntimeException("close inner client exception : ", e);
            } catch (TimeoutException e) {
                logger.error("close inner client exception : ", e);
                throw new RuntimeException("close inner client exception : ", e);
            } catch (Exception e) {
                logger.error("close inner client exception : ", e);
                throw new RuntimeException("close inner client exception : ", e);
            }
        }
    }

    public boolean isOpen() {
        return this.isOpen.get();
    }

}
