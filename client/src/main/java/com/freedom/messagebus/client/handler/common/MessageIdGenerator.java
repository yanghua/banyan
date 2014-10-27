package com.freedom.messagebus.client.handler.common;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.model.HandlerModel;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.common.UUIDGenerator;
import com.freedom.messagebus.common.message.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class MessageIdGenerator extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(MessageIdGenerator.class);

    @Override
    public void init(@NotNull HandlerModel handlerModel) {
        super.init(handlerModel);
    }

    @Override
    public void handle(@NotNull MessageContext context, @NotNull IHandlerChain chain) {
        Message[] msgs = context.getMessages();
        //if non-response
        if (!(context.getCarryType().equals(MessageCarryType.RESPONSE)
            || context.getCarryType().equals(MessageCarryType.PUBLISH)
            || context.getCarryType().equals(MessageCarryType.BROADCAST))) {
            for (Message msg : msgs) {
                UUIDGenerator generator = new UUIDGenerator(context.getQueueNode().getGeneratedId(), CONSTS.DEFAULT_DATACENTER_ID_FOR_UUID);
                logger.debug("message id is : " + generator.nextId());
                msg.getMessageHeader().setMessageId(generator.nextId());
            }
        } else {
            Random random = new Random();
            for (Message msg : msgs) {
                UUIDGenerator generator = new UUIDGenerator(random.nextInt(31), CONSTS.DEFAULT_DATACENTER_ID_FOR_UUID);
                logger.debug("message id is : " + generator.nextId());
                msg.getMessageHeader().setMessageId(generator.nextId());
            }
        }

        chain.handle(context);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
