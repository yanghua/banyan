package com.freedom.messagebus.client.handler.produce;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.model.HandlerModel;
import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.common.UUIDGenerator;
import com.freedom.messagebus.common.message.Message;
import org.jetbrains.annotations.NotNull;

public class MessageIdGenerator extends AbstractHandler {

    @Override
    public void init(@NotNull HandlerModel handlerModel) {
        super.init(handlerModel);
    }

    @Override
    public void handle(@NotNull MessageContext context, @NotNull IHandlerChain chain) {
        Message[] msgs = context.getMessages();
        for (Message msg : msgs) {
            UUIDGenerator generator = new UUIDGenerator(context.getQueueNode().getGeneratedId(), CONSTS.DEFAULT_DATACENTER_ID_FOR_UUID);
            msg.getMessageHeader().setMessageId(generator.nextId());
        }

        chain.handle(context);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
