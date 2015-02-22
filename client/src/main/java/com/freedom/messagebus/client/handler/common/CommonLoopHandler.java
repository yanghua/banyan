package com.freedom.messagebus.client.handler.common;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.model.MessageFactory;
import com.freedom.messagebus.client.message.model.MessageType;
import com.freedom.messagebus.client.message.transfer.IMessageBodyTransfer;
import com.freedom.messagebus.client.message.transfer.MessageBodyTransferFactory;
import com.freedom.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Created by yanghua on 2/22/15.
 */
public abstract class CommonLoopHandler extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(CommonLoopHandler.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        QueueingConsumer currentConsumer = (QueueingConsumer) context.getOtherParams().get("consumer");
        try {
            while (true) {
                QueueingConsumer.Delivery delivery = currentConsumer.nextDelivery();

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

                context.setConsumedMsg(msg);

                process(context);
            }
        } catch (InterruptedException e) {
            logger.info("[run] close the consumer's message handler!");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            chain.handle(context);
        }
    }

    public abstract void process(MessageContext msgContext);

    private void initMessage(Message msg, MessageType msgType, AMQP.BasicProperties properties, byte[] bodyData) {
        MessageHeaderTransfer.unbox(properties, msgType, msg.getMessageHeader());

        IMessageBodyTransfer msgBodyProcessor = MessageBodyTransferFactory.createMsgBodyProcessor(msgType);
        msg.setMessageBody(msgBodyProcessor.unbox(bodyData));
    }
}
