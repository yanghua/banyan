package com.freedom.messagebus.client.handler.consume;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.common.message.MessageFactory;
import com.freedom.messagebus.common.message.MessageType;
import com.freedom.messagebus.interactor.message.IMessageBodyProcessor;
import com.freedom.messagebus.interactor.message.MessageBodyProcessorFactory;
import com.freedom.messagebus.interactor.message.MessageHeaderProcessor;
import com.freedom.messagebus.interactor.proxy.ProxyConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.GetResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SyncConsumer extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(SyncConsumer.class);

    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    @Override
    public void handle(@NotNull MessageContext context, @NotNull IHandlerChain chain) {
        if (context.isSync()) {
            List<Message> consumeMsgs = new ArrayList<>(context.getConsumeMsgNum());
            context.setConsumeMsgs(consumeMsgs);
            try {
                int countDown = context.getConsumeMsgNum();
                while (countDown-- > 0) {
                    GetResponse response = ProxyConsumer.consumeSingleMessage(context.getChannel(),
                                                                              context.getQueueNode().getValue());

                    if (response == null)
                        continue;

                    AMQP.BasicProperties properties = response.getProps();
                    byte[] msgBody = response.getBody();

                    context.getChannel().basicAck(response.getEnvelope().getDeliveryTag(), false);

                    String msgTypeStr = properties.getType();
                    if (msgTypeStr == null || msgTypeStr.isEmpty()) {
                        logger.error("[handle] message type is null or empty");
                        continue;
                    }

                    MessageType msgType = null;
                    try {
                        msgType = MessageType.lookup(msgTypeStr);
                    } catch (UnknownError unknownError) {
                        throw new RuntimeException("unknown message type : " + msgTypeStr);
                    }
                    Message msg = MessageFactory.createMessage(msgType);
                    initMessage(msg, msgType, properties, msgBody);
                    consumeMsgs.add(msg);
                }
            } catch (IOException | RuntimeException e) {
                logger.error("[handle] occurs a Exception " + e.getMessage());
            } finally {
                //destroy channel
                context.getDestroyer().destroy(context.getChannel());
            }
        }

        chain.handle(context);
    }

    private void initMessage(Message msg, MessageType msgType, AMQP.BasicProperties properties, byte[] bodyData) {
        MessageHeaderProcessor.unbox(properties, msgType, msg.getMessageHeader());

        IMessageBodyProcessor msgBodyProcessor = MessageBodyProcessorFactory.createMsgBodyProcessor(msgType);
        msg.setMessageBody(msgBodyProcessor.unbox(bodyData));
    }
}
