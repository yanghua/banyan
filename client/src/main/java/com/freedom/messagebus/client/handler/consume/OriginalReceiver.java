package com.freedom.messagebus.client.handler.consume;

import com.freedom.messagebus.client.IChannelDestroyer;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.common.message.MessageType;
import com.freedom.messagebus.interactor.message.IMessageBodyProcessor;
import com.freedom.messagebus.interactor.message.MessageBodyProcessorFactory;
import com.freedom.messagebus.interactor.message.MessageHeaderProcessor;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * the original message receiver it's a handler and also a service
 * it start a while-true loop to receive the message from the queues belong rabbitmq-server's
 */
public class OriginalReceiver extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(OriginalReceiver.class);


    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    @Override
    public void handle(@NotNull MessageContext context,
                       @NotNull IHandlerChain chain) {
        if (!context.isSync()) {
            ReceiveEventLoop eventLoop = new ReceiveEventLoop();
            eventLoop.setChain(chain);
            eventLoop.setContext(context);
            eventLoop.setChannelDestroyer(context.getDestroyer());
            eventLoop.setCurrentConsumer((QueueingConsumer) context.getOtherParams().get("consumer"));
            context.setReceiveEventLoop(eventLoop);

            eventLoop.startEventLoop();
        } else {
            chain.handle(context);
        }
    }

    public class ReceiveEventLoop implements Runnable {

        @NotNull
        private QueueingConsumer currentConsumer;

        @NotNull
        private IChannelDestroyer channelDestroyer;
        @NotNull
        private MessageContext    context;
        @NotNull
        private IHandlerChain     chain;
        @NotNull
        private Thread            currentThread;

        private ReceiveEventLoop() {
            this.currentThread = new Thread(this);
            this.currentThread.setDaemon(false);
        }

        @Override
        public void run() {
            try {
                while (true) {
                    QueueingConsumer.Delivery delivery = this.currentConsumer.nextDelivery();

                    AMQP.BasicProperties properties = delivery.getProperties();
                    byte[] msgBody = delivery.getBody();

                    context.getChannel().basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                    String msgTypeStr = properties.getType();
                    if (msgTypeStr == null || msgTypeStr.isEmpty()) {
                        logger.error("[run] message type is null or empty");
                        continue;
                    }

                    MessageType msgType = MessageType.lookup(msgTypeStr);
                    Message msg = new Message();
                    initMessage(msg, msgType, properties, msgBody);

                    this.context.setConsumedMsg(msg);
                    this.chain.handle(this.context);
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

        public void startEventLoop() {
            this.currentThread.start();
        }

        /**
         * shut down launch a interrupt to itself
         */
        public void shutdown() {
            this.channelDestroyer.destroy(context.getChannel());
            this.currentThread.interrupt();
        }

        public boolean isAlive() {
            return this.currentThread.isAlive();
        }

        protected long getThreadID() {
            return this.currentThread.getId();
        }

        private void initMessage(Message msg, MessageType msgType, AMQP.BasicProperties properties, byte[] bodyData) {
            msg.setMessageHeader(MessageHeaderProcessor.unbox(properties, msgType));
            msg.setMessageType(msgType);

            IMessageBodyProcessor msgBodyProcessor = MessageBodyProcessorFactory.createMsgBodyProcessor(msgType);
            msg.setMessageBody(msgBodyProcessor.unbox(bodyData));
        }

        @NotNull
        public QueueingConsumer getCurrentConsumer() {
            return currentConsumer;
        }

        public void setCurrentConsumer(@NotNull QueueingConsumer currentConsumer) {
            this.currentConsumer = currentConsumer;
        }

        @NotNull
        public IChannelDestroyer getChannelDestroyer() {
            return channelDestroyer;
        }

        public void setChannelDestroyer(@NotNull IChannelDestroyer channelDestroyer) {
            this.channelDestroyer = channelDestroyer;
        }

        @NotNull
        public MessageContext getContext() {
            return context;
        }

        public void setContext(@NotNull MessageContext context) {
            this.context = context;
        }

        @NotNull
        public IHandlerChain getChain() {
            return chain;
        }

        public void setChain(@NotNull IHandlerChain chain) {
            this.chain = chain;
        }
    }
}
