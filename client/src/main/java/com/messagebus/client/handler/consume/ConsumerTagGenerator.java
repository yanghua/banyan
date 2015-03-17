package com.messagebus.client.handler.consume;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.model.HandlerModel;
import com.messagebus.common.RandomHelper;

public class ConsumerTagGenerator extends AbstractHandler {

    private static final String CONSUMER_TAG_PREFIX = "consumer.tag.";

    @Override
    public void init(HandlerModel handlerModel) {

    }

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        String tag = CONSUMER_TAG_PREFIX + RandomHelper.randomNumberAndCharacter(6);
        context.setConsumerTag(tag);
        chain.handle(context);
    }

    @Override
    public void destroy() {

    }
}
