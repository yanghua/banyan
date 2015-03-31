package com.messagebus.client;

import com.messagebus.business.exchanger.ExchangerManager;
import com.messagebus.client.core.config.ConfigManager;
import com.messagebus.common.ExceptionHelper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by yanghua on 3/1/15.
 */
abstract class InnerClient {

    private static final Log logger = LogFactory.getLog(Messagebus.class);

    //inject by reflector
    private ExchangerManager exchangeManager;
    private ConfigManager    configManager;
    private Connection       connection;

    private   Channel                 channel;
    protected GenericContext          context;
    protected IMessageReceiveListener notificationListener;

    private AtomicBoolean isOpen = new AtomicBoolean(false);

    public InnerClient() {

    }

    private void open() throws MessagebusConnectedFailedException {
        if (this.isOpen())
            return;

        try {
            this.channel = this.connection.createChannel();
            context = new GenericContext();
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
        try {
            if (this.channel != null && this.channel.isOpen())
                this.channel.close();

            this.isOpen.compareAndSet(true, false);
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "close");
            throw new RuntimeException(e);
        }
    }

    public boolean isOpen() {
        return this.isOpen.get();
    }

}
