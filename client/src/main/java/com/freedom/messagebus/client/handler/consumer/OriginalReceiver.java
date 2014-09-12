package com.freedom.messagebus.client.handler.consumer;

import com.freedom.messagebus.client.IChannelDestroyer;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.MessageCarryHandlerChain;
import com.freedom.messagebus.client.model.MessageFormat;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * the original message receiver it's a handler and also a service
 * it start a while-true loop to receive the message from the queues belong rabbitmq-server's
 */
public class OriginalReceiver extends AbstractHandler implements Runnable {

    private static final Log logger = LogFactory.getLog(OriginalReceiver.class);

    @NotNull
    private Thread          currentThread;
    @NotNull
    private DefaultConsumer currentConsumer;
    @NotNull
    private Channel         currentChannel;

    @NotNull
    private IChannelDestroyer channelDestroyer;
    @NotNull
    private MessageContext    context;
    @NotNull
    private IHandlerChain     chain;


    public OriginalReceiver() {
        currentThread = new Thread(this);
        this.currentThread.setDaemon(false);
    }

    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    @Override
    public void handle(@NotNull MessageContext context,
                       @NotNull IHandlerChain chain) {
        context.setReceiver(this);
        this.chain = chain;
        this.context = context;
        this.currentChannel = context.getChannel();
        this.channelDestroyer = context.getDestroyer();
        this.currentConsumer = (QueueingConsumer) context.getOtherParams().get("consumer");

        //enable repeat handler at this handler
        MessageCarryHandlerChain carryHandlerChain = (MessageCarryHandlerChain) this.chain;
        carryHandlerChain.setEnableRepeatBeforeNextHandler(true);

        this.currentThread.start();
    }

    public void run() {
        QueueingConsumer consumer = null;
        if (this.currentConsumer instanceof QueueingConsumer) {
            consumer = (QueueingConsumer) this.currentConsumer;
        } else {
            throw new RuntimeException("unsupported type of consumer");
        }

        try {
            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                this.context.setMsgFormat(MessageFormat.lookup(delivery.getProperties().getContentType()));
                this.context.setConsumedMsgBytes(delivery.getBody());

                this.chain.handle(this.context);

                this.currentChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        } catch (InterruptedException e) {
            logger.info("[run] close the consumer's message handler!");
        } catch (IOException e) {
            logger.error("[run] occurs a IOException : " + e.getMessage());
        } catch (ConsumerCancelledException e) {
            logger.info("[run] the consumer has been canceled ");
        } catch (Exception e) {
            logger.error("[run] occurs a Exception : " + e.getMessage());
        }

        logger.info("******** thread id " + this.getThreadID() + " quit from message receiver ********");
    }

    /**
     * shut down launch a interrupt to itself
     */
    public void shutdown() {
        this.channelDestroyer.destroy(this.currentChannel);
        this.currentThread.interrupt();
    }

    protected long getThreadID() {
        return this.currentThread.getId();
    }
}
