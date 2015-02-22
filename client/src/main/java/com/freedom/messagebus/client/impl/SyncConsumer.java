package com.freedom.messagebus.client.impl;

import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.client.AbstractMessageCarryer;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.MessageCarryHandlerChain;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.model.MessageFactory;
import com.freedom.messagebus.client.message.model.MessageType;
import com.freedom.messagebus.client.message.transfer.IMessageBodyTransfer;
import com.freedom.messagebus.client.message.transfer.MessageBodyTransferFactory;
import com.freedom.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.ExceptionHelper;
import com.freedom.messagebus.interactor.proxy.ProxyConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.GetResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * a sync consumer.
 */
public class SyncConsumer extends AbstractMessageCarryer {

    private static final Log logger = LogFactory.getLog(SyncConsumer.class);


    public SyncConsumer() {
    }

    public List<Message> consume(String appName, int expectedNum) {
        final MessageContext ctx = new MessageContext();
        ctx.setCarryType(MessageCarryType.CONSUME);
        ctx.setAppId(this.getContext().getAppId());

        ctx.setSourceNode(ConfigManager.getInstance().getAppIdQueueMap().get(this.getContext().getAppId()));
        Node node = ConfigManager.getInstance().getQueueNodeMap().get(appName);
        ctx.setTargetNode(node);

        ctx.setPool(this.getContext().getPool());
        ctx.setConnection(this.getContext().getConnection());
        ctx.setConsumeMsgNum(expectedNum);
        ctx.setSync(true);

        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.CONSUME,
                                                         this.getContext());
        //launch pipeline
        this.handlerChain.handle(ctx);

        //sync consume
        this.syncConsume(ctx, handlerChain);

        return ctx.getConsumeMsgs();
    }

    private void syncConsume(MessageContext context, IHandlerChain chain) {
        List<Message> consumeMsgs = new ArrayList<>(context.getConsumeMsgNum());
        context.setConsumeMsgs(consumeMsgs);
        try {
            int countDown = context.getConsumeMsgNum();
            while (countDown-- > 0) {
                GetResponse response = ProxyConsumer.consumeSingleMessage(context.getChannel(),
                                                                          context.getTargetNode().getValue());

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
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "handle");
            throw new RuntimeException(e);
        }
    }

    private void initMessage(Message msg,
                             MessageType msgType,
                             AMQP.BasicProperties properties,
                             byte[] bodyData) {
        MessageHeaderTransfer.unbox(properties, msgType, msg.getMessageHeader());

        IMessageBodyTransfer msgBodyProcessor = MessageBodyTransferFactory.createMsgBodyProcessor(msgType);
        msg.setMessageBody(msgBodyProcessor.unbox(bodyData));
    }
}
