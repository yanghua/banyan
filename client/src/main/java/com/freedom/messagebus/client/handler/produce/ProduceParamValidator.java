package com.freedom.messagebus.client.handler.produce;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.ParamValidateFailedException;
import com.freedom.messagebus.client.handler.common.AbstractParamValidator;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.message.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class ProduceParamValidator extends AbstractParamValidator {

    private static final Log logger = LogFactory.getLog(ProduceParamValidator.class);

    @Override
    public void handle(@NotNull MessageContext context, @NotNull IHandlerChain chain) {
        super.handle(context, chain);

        if (context.getCarryType().equals(MessageCarryType.PRODUCE)) {
            if (context.getQueueNode().getRoutingKey() == null || context.getQueueNode().getRoutingKey().isEmpty()) {
                logger.error("[handle] the context field : queueNode is illegal. the routingkey can not be null or empty");
                throw new ParamValidateFailedException("[handle] the context field : queueNode is illegal. " +
                                                           "the routingkey can not be null or empty");
            }

            if (context.getQueueNode() == null) {
                logger.error("[handle] the context field : queueNode is illegal. it can not be null");
                throw new ParamValidateFailedException("the context field : queueNode is illegal. " +
                                                           "it can not be null ");
            }

            if (context.getQueueNode().getType() == 0) {
                logger.error("[handle] the context field : queueNode is illegal. it must be a queue type");
                throw new ParamValidateFailedException("[handle] the context field : queueNode is illegal. " +
                                                           "it must be a queue type");
            }

            if (context.getQueueNode().getName() == null || context.getQueueNode().getName().isEmpty()) {
                logger.error("[handle] the context field : queueNode is illegal. the name can not be null or empty");
                throw new ParamValidateFailedException("[handle] the context field : queueNode is illegal. " +
                                                           "the name can not be null or empty");
            }

            this.validateMessagesAppId(context);
            this.validateMessagesTimestamp(context);
        }

        chain.handle(context);
    }

    private void validateMessagesAppId(@NotNull MessageContext context) {
        for (Message msg : context.getMessages()) {
            if (msg.getMessageHeader().getAppId() == null || msg.getMessageHeader().getAppId().isEmpty())
                msg.getMessageHeader().setAppId(context.getAppId());
        }
    }

    private void validateMessagesTimestamp(@NotNull MessageContext context) {
        Date currentDate = new Date();
        for (Message msg : context.getMessages()) {
            if (msg.getMessageHeader().getTimestamp() == null)
                msg.getMessageHeader().setTimestamp(currentDate);
        }
    }
}
