package com.freedom.messagebus.client.handler.common;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.model.HandlerModel;
import com.freedom.messagebus.common.Constants;
import com.freedom.messagebus.common.UUIDGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Random;

public class MessageIdGenerator extends AbstractHandler {

    private static final Log    logger = LogFactory.getLog(MessageIdGenerator.class);
    private static final Random random = new Random();

    @Override
    public void init(HandlerModel handlerModel) {
        super.init(handlerModel);
    }

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        Message[] msgs = context.getMessages();
        for (Message msg : msgs) {
            UUIDGenerator generator = new UUIDGenerator(random.nextInt(31), Constants.DEFAULT_DATACENTER_ID_FOR_UUID);
            logger.debug("message id is : " + generator.nextId());
            msg.getMessageHeader().setMessageId(generator.nextId());
        }

        chain.handle(context);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
