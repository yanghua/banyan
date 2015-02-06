package com.freedom.messagebus.client.handler.consume;

import com.freedom.messagebus.client.IChannelDestroyer;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.MessageCarryHandlerChain;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.model.MessageFactory;
import com.freedom.messagebus.client.message.model.MessageType;
import com.freedom.messagebus.client.message.transfer.IMessageBodyTransfer;
import com.freedom.messagebus.client.message.transfer.MessageBodyTransferFactory;
import com.freedom.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    public void handle(MessageContext context,
                       IHandlerChain chain) {
        if (!context.isSync()) {
            ReceiveEventLoop eventLoop = new ReceiveEventLoop();
            eventLoop.setChain(chain);
            eventLoop.setContext(context);
            eventLoop.setChannelDestroyer(context.getDestroyer());
            eventLoop.setCurrentConsumer((QueueingConsumer) context.getOtherParams().get("consumer"));
            context.setReceiveEventLoop(eventLoop);

            //repeat current handler
            ((MessageCarryHandlerChain) chain).setEnableRepeatBeforeNextHandler(true);

            eventLoop.startEventLoop();
        } else {
            chain.handle(context);
        }
    }

    public static class ReceiveEventLoop implements Runnable {


        private QueueingConsumer currentConsumer;


        private IChannelDestroyer channelDestroyer;

        private MessageContext context;

        private IHandlerChain chain;

        private Thread currentThread;

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

                    MessageType msgType = null;
                    try {
                        msgType = MessageType.lookup(msgTypeStr);
                    } catch (UnknownError unknownError) {
                        throw new RuntimeException("unknown message type :" + msgTypeStr);
                    }
                    Message msg = MessageFactory.createMessage(msgType);
                    initMessage(msg, msgType, properties, msgBody);

                    this.context.setConsumedMsg(msg);
                    this.chain.handle(this.context);
                }
            } catch (InterruptedException e) {
                logger.info("[run] close the consumer's message handler!");
            } catch (IOException e) {
                logger.error("[run] occurs a IOException : " + e.getMessage());
                this.shutdown();
            } catch (ConsumerCancelledException e) {
                logger.info("[run] the consumer has been canceled ");
                this.shutdown();
            } catch (Exception e) {
                logger.error("[run] occurs a Exception : " + e.getMessage());
                this.shutdown();
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
            MessageHeaderTransfer.unbox(properties, msgType, msg.getMessageHeader());

            IMessageBodyTransfer msgBodyProcessor = MessageBodyTransferFactory.createMsgBodyProcessor(msgType);
            msg.setMessageBody(msgBodyProcessor.unbox(bodyData));
        }


        public QueueingConsumer getCurrentConsumer() {
            return currentConsumer;
        }

        public void setCurrentConsumer(QueueingConsumer currentConsumer) {
            this.currentConsumer = currentConsumer;
        }


        public IChannelDestroyer getChannelDestroyer() {
            return channelDestroyer;
        }

        public void setChannelDestroyer(IChannelDestroyer channelDestroyer) {
            this.channelDestroyer = channelDestroyer;
        }


        public MessageContext getContext() {
            return context;
        }

        public void setContext(MessageContext context) {
            this.context = context;
        }


        public IHandlerChain getChain() {
            return chain;
        }

        public void setChain(IHandlerChain chain) {
            this.chain = chain;
        }
    }
}
