package com.freedom.messagebus.interactor.rabbitmq;

import com.freedom.messagebus.common.AbstractInitializer;
import com.freedom.messagebus.common.RouterType;
import com.rabbitmq.client.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ExchangeManager extends AbstractInitializer {

    private static Log logger = LogFactory.getLog(ExchangeManager.class);

    private static volatile ExchangeManager instance;

    private ExchangeManager(@NotNull String host) {
        super(host);
    }

    public static ExchangeManager defaultManager(@NotNull String host) {
        if (instance == null) {
            synchronized (TopologyManager.class) {
                if (instance == null) {
                    instance = new ExchangeManager(host);
                }
            }
        }

        return instance;
    }

    public void create(@NotNull String exchangeName, RouterType routerType) throws IOException {
        super.init();
        this.channel.exchangeDeclare(exchangeName, routerType.toString(), true);
        super.close();
    }

    public void create(@NotNull String exchangeName,
                       RouterType routerType,
                       String bindTo,
                       String routingKey) throws IOException {
        super.init();
        this.channel.exchangeDeclare(exchangeName, routerType.toString(), true);

        //bind
        if (bindTo != null && !bindTo.isEmpty() && this.innerExists(bindTo, channel))
            this.channel.exchangeBind(exchangeName, bindTo, routingKey);

        super.close();
    }

    public void bind(@NotNull String exchangeName, @NotNull String bindTo, String routingKey) throws IOException {
        super.init();
        if (!this.innerExists(exchangeName, channel) || !this.innerExists(bindTo, channel)) {
            logger.error("exchange : " + exchangeName + " or " + bindTo + "is not exists");
            throw new IOException("exchange : " + exchangeName + " or " + bindTo + "is not exists");
        }

        this.channel.exchangeBind(exchangeName, bindTo, routingKey);
        super.close();
    }

    public void unbind(@NotNull String exchangeName, @NotNull String unbindTo, String routingKey) throws IOException {
        super.init();
        if (!this.innerExists(exchangeName, channel) || !this.innerExists(unbindTo, channel)) {
            logger.error("exchange : " + exchangeName + " or " + unbindTo + "is not exists");
            throw new IOException("exchange : " + exchangeName + " or " + unbindTo + "is not exists");
        }

        this.channel.exchangeUnbind(exchangeName, unbindTo, routingKey);
        super.close();
    }

    public void delete(@NotNull String exchangeName) throws IOException {
        super.init();
        if (!this.innerExists(exchangeName, channel)) {
            logger.error("exchange : " + exchangeName + " is not exists");
            throw new IOException("exchange : " + exchangeName + " is not exists");
        }

        this.channel.exchangeDelete(exchangeName);
        super.close();
    }

    public boolean exchangeExists(@NotNull String exchangeName) throws IOException {
        super.init();
        boolean result = true;
        try {
            this.channel.exchangeDeclarePassive(exchangeName);
        } catch (IOException e) {
            result = false;
        }
        super.close();

        return result;
    }

    private boolean innerExists(@NotNull String exchangeName, @NotNull Channel outerChannel) {
        boolean result = true;
        try {
            outerChannel.exchangeDeclarePassive(exchangeName);
        } catch (IOException e) {
            result = false;
        }

        return result;
    }

}
