package com.messagebus.client.handler.common;

import com.google.common.base.Strings;
import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.common.ExceptionHelper;
import com.messagebus.common.compress.CompressorFactory;
import com.messagebus.common.compress.ICompressor;
import com.rabbitmq.client.QueueingConsumer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;

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

                final Message msg = MessageFactory.createMessage(delivery);

                if (msg == null) continue;

                if (msg.getMessageType().equals(MessageType.QueueMessage)) {
                    this.doUncompress(context, msg);
                }

                context.setConsumeMsgs(new ArrayList<Message>(1) {{
                    this.add(msg);
                }});

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

    public void doUncompress(MessageContext context, Message receivedMsg) {
        String compressAlgo = context.getSourceNode().getCompress();
        if (!Strings.isNullOrEmpty(compressAlgo)) {
            ICompressor compressor = CompressorFactory.createCompressor(compressAlgo);
            if (compressor != null) {
                receivedMsg.setContent(compressor.uncompress(receivedMsg.getContent()));
            } else {
                logger.error("the target node with name : " + context.getTargetNode().getName()
                                 + " configured a compress named : " + compressAlgo
                                 + " but client can not get the compressor instance. ");
            }
        }
    }

}
