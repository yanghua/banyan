package com.messagebus.client;

import com.messagebus.common.ExceptionHelper;
import com.messagebus.interactor.pubsub.PubsuberManager;
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

    //inject by reflector
    private PubsuberManager exchangeManager;
    private ConfigManager   configManager;
    private Connection      connection;

    private   Channel                 channel;
    protected GenericContext          context;
    protected IMessageReceiveListener notificationListener;

    private AtomicBoolean isOpen = new AtomicBoolean(false);

    public InnerClient() {
        context = new GenericContext();
    }

    private void open() throws MessagebusConnectedFailedException {
        if (this.isOpen())
            return;

        try {
            this.channel = this.connection.createChannel();
            context.setChannel(this.channel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        context.setConfigManager(this.configManager);
        context.setConnection(this.connection);

        this.isOpen.compareAndSet(false, true);
    }

    private void close() {
        //release all resource
        synchronized (this.channel) {
            try {
                if (this.channel != null && this.channel.isOpen())
                    this.channel.close();

                this.isOpen.compareAndSet(true, false);
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
