package com.messagebus.client.handler.common;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.common.ExceptionHelper;
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

                Message msg = MessageFactory.createMessage(delivery);

                if (msg == null) continue;

                context.setConsumedMsg(msg);

                try {
                    if (msg.getMessageType().equals(MessageType.BroadcastMessage) && context.getNoticeListener() != null) {
                        IMessageReceiveListener noticeListener = context.getNoticeListener();
                        noticeListener.onMessage(msg);
                    } else {
                        process(context);
                    }
                } catch (Exception e) {
                    ExceptionHelper.logException(logger, e, "outer of message handler");
                }
            }
        } catch (InterruptedException e) {
            logger.info("[run] close the consumer's message handler!");
        } catch (Exception e) {
            ExceptionHelper.logException(logger, e, "common loop handler");
        } finally {
            //close the consume based on this channel
            synchronized (context.getChannel()) {
                try {
                    if (context.getChannel().isOpen()) {
                        context.getChannel().basicCancel(context.getConsumerTag());
                    }
                } catch (IOException e1) {
                    ExceptionHelper.logException(logger, e1, "cancel a consumer");
                }
            }
            chain.handle(context);
        }
    }

    public abstract void process(MessageContext msgContext);

//    private void initMessage(Message msg, AMQP.BasicProperties properties, byte[] bodyData) {
//        MessageHeaderTransfer.unbox(properties, msg);
//        msg.setContent(bodyData);
//    }
}
