package com.messagebus.client.handler.request;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.client.model.HandlerModel;
import com.messagebus.interactor.proxy.ProxyConsumer;
import com.messagebus.interactor.rabbitmq.QueueManager;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;

public class BlockedAndTimeoutResponser extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(BlockedAndTimeoutResponser.class);

    /**
     * do some init things (optional implementation)
     *
     * @param handlerModel the model of handler element
     */
    @Override
    public void init(HandlerModel handlerModel) {

    }

    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        String correlationId = context.getMessages()[0].getCorrelationId();

        try {
            //just receive one
            QueueingConsumer consumer = ProxyConsumer.consume(context.getChannel(),
                                                              correlationId,
                                                              context.getConsumerTag());
            QueueingConsumer.Delivery delivery = consumer.nextDelivery(context.getTimeout() * 1000);

            //timeout
            if (delivery == null) {
                context.setIsTimeout(true);
                return;
            }

//            context.getChannel().basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            final Message msg = MessageFactory.createMessage(delivery);

            if (msg == null) return;

            context.setConsumeMsgs(new ArrayList<Message>(1) {{
                this.add(msg);
            }});
        } catch (IOException e) {
            logger.error("[handle] occurs a exception : ", e);
        } catch (InterruptedException e) {
        } finally {
            //delete temp queue
            QueueManager queueManager = QueueManager.defaultQueueManager(context.getHost());
            String msgIdStr = String.valueOf(correlationId);
            try {
                if (queueManager.exists(msgIdStr)) {
                    queueManager.delete(msgIdStr);
                }
            } catch (IOException e) {
                logger.error("[handle] finally block occurs a IOException : ", e);
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
        MessageHeaderTransfer.unbox(properties, msg);
        msg.setContent(bodyData);
    }
}
