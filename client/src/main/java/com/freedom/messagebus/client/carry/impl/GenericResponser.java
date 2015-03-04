package com.freedom.messagebus.client.carry.impl;

import com.freedom.messagebus.client.AbstractMessageCarryer;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.carry.IResponser;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.MessageCarryHandlerChain;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.transfer.IMessageBodyTransfer;
import com.freedom.messagebus.client.message.transfer.MessageBodyTransferFactory;
import com.freedom.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.ExceptionHelper;
import com.freedom.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class GenericResponser extends AbstractMessageCarryer implements IResponser {

    private static final Log logger = LogFactory.getLog(GenericResponser.class);

    public GenericResponser() {
    }

    /**
     * response a temp message to a named queue
     *
     * @param msg       the entity of message
     * @param queueName the temp queue name
     */
    @Override
    public void responseTmpMessage(Message msg, String queueName) {
        final MessageContext ctx = initMessageContext();
        ctx.setCarryType(MessageCarryType.RESPONSE);
        ctx.setSourceNode(this.getContext().getConfigManager()
                              .getAppIdQueueMap().get(this.getContext().getAppId()));
        ctx.setMessages(new Message[]{msg});
        ctx.setTempQueueName(queueName);

        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.RESPONSE, this.getContext());
        //launch pipeline
        this.handlerChain.handle(ctx);

        //consume
        this.genericResponse(ctx, handlerChain);
    }

    private void genericResponse(MessageContext context, IHandlerChain chain) {
        Message responseMsg = context.getMessages()[0];
        IMessageBodyTransfer msgBodyProcessor =
            MessageBodyTransferFactory.createMsgBodyProcessor(responseMsg.getMessageType());
        byte[] msgBody = msgBodyProcessor.box(responseMsg.getMessageBody());
        AMQP.BasicProperties properties = MessageHeaderTransfer.box(responseMsg.getMessageHeader());
        try {
            ProxyProducer.produce("",
                                  context.getChannel(),
                                  context.getTempQueueName(),
                                  msgBody,
                                  properties);
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "genericResponse");
            throw new RuntimeException(e);
        }
    }

}
