package com.freedom.messagebus.client.handler.produce;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.model.MsgBytes;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * the real producer
 */
public class RealProducer extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(RealProducer.class);

    private static final AMQP.BasicProperties.Builder BASIC_PROPERTIES_BUILDER = new AMQP.BasicProperties.Builder();


    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    @Override
    public void handle(@NotNull MessageContext context,
                       @NotNull IHandlerChain chain) {
        AMQP.BasicProperties properties = buildMsgFormatProperty(context);

        try {
            if (context.isEnableTransaction()) {
                this.transactionProduce(properties, context, chain);
            } else {
                this.normalProduce(properties, context, chain);
            }

            chain.handle(context);
        } catch (IOException e) {
            logger.error("[handle] occurs a IOException : " + e.getMessage());
        } finally {
            context.getDestroyer().destroy(context.getChannel());
        }
    }

    @NotNull
    private AMQP.BasicProperties buildMsgFormatProperty(@NotNull MessageContext context) {
        AMQP.BasicProperties properties = BASIC_PROPERTIES_BUILDER
            .deliveryMode(2)
            .contentType(context.getMsgFormat().stringValue())
            .build();

        return properties;
    }

    private void normalProduce(@NotNull AMQP.BasicProperties properties, @NotNull MessageContext context,
                               @NotNull IHandlerChain chain) throws IOException {
        Channel channel = context.getChannel();

        for (MsgBytes msgBytes : context.getMsgBytes()) {
            channel.basicPublish("exchange.proxy", context.getRuleValue(), properties, msgBytes.getMsgBytes());
        }
    }

    private void transactionProduce(@NotNull AMQP.BasicProperties properties, @NotNull MessageContext context,
                                    @NotNull IHandlerChain chain) throws IOException {
        Channel channel = context.getChannel();

        //transaction begin
        channel.txSelect();

        for (MsgBytes msgBytes : context.getMsgBytes()) {
            channel.basicPublish("exchange.proxy", context.getRuleValue(), properties, msgBytes.getMsgBytes());

            //commit every message with wrapped a transaction
            //NOTE: it is almost for security! Not for normal, because of bad performance!!!
            channel.txCommit();
        }
    }

}
