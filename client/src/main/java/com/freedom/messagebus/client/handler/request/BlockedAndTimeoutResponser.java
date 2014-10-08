package com.freedom.messagebus.client.handler.request;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.model.HandlerModel;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.common.message.MessageType;
import com.freedom.messagebus.interactor.message.IMessageBodyProcessor;
import com.freedom.messagebus.interactor.message.MessageBodyProcessorFactory;
import com.freedom.messagebus.interactor.message.MessageHeaderProcessor;
import com.freedom.messagebus.interactor.proxy.ProxyConsumer;
import com.freedom.messagebus.interactor.rabbitmq.QueueManager;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class BlockedAndTimeoutResponser extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(BlockedAndTimeoutResponser.class);

    /**
     * do some init things (optional implementation)
     *
     * @param handlerModel the model of handler element
     */
    @Override
    public void init(@NotNull HandlerModel handlerModel) {

    }

    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    @Override
    public void handle(@NotNull MessageContext context, @NotNull IHandlerChain chain) {
        long msgId = context.getMessages()[0].getMessageHeader().getMessageId();

        try {
            //just receive one
            QueueingConsumer consumer = ProxyConsumer.consume(context.getChannel(),
                                                              String.valueOf(msgId),
                                                              context.getConsumerTag());
            QueueingConsumer.Delivery delivery = consumer.nextDelivery(context.getTimeout());

            //timeout
            if (delivery == null) {
                context.setIsTimeout(true);
                return;
            }

            AMQP.BasicProperties properties = delivery.getProperties();
            byte[] msgBody = delivery.getBody();

            context.getChannel().basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            //destroy queue
            QueueManager.defaultQueueManager(context.getHost()).delete(String.valueOf(msgId));

            String msgTypeStr = properties.getType();
            if (msgTypeStr == null || msgTypeStr.isEmpty()) {
                logger.error("[run] message type is null or empty");
            }

            MessageType msgType = MessageType.lookup(msgTypeStr);
            Message msg = new Message();
            initMessage(msg, msgType, properties, msgBody);
            context.setConsumedMsg(msg);
        } catch (IOException | InterruptedException e) {
            logger.error("[handle] occurs a exception : " + e.getMessage());
        } finally {
            //delete temp queue
            QueueManager queueManager = QueueManager.defaultQueueManager(context.getHost());
            String msgIdStr = String.valueOf(msgId);
            try {
                if (queueManager.exists(msgIdStr)) {
                    queueManager.delete(msgIdStr);
                }
            } catch (IOException e) {
                logger.error("[handle] finally block occurs a IOException : " + e.getMessage());
            } finally {
                //destroy channel
                context.getDestroyer().destroy(context.getChannel());
            }
        }
    }

    /**
     * resource clear after the handler would not be used
     */
    @Override
    public void destroy() {

    }

    private void initMessage(Message msg, MessageType msgType, AMQP.BasicProperties properties, byte[] bodyData) {
        msg.setMessageHeader(MessageHeaderProcessor.unbox(properties, msgType));
        msg.setMessageType(msgType);

        IMessageBodyProcessor msgBodyProcessor = MessageBodyProcessorFactory.createMsgBodyProcessor(msgType);
        msg.setMessageBody(msgBodyProcessor.unbox(bodyData));
    }
}
