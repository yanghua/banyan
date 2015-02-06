package com.freedom.messagebus.client.handler.response;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.ParamValidateFailedException;
import com.freedom.messagebus.client.handler.common.AbstractParamValidator;
import com.freedom.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ResponseParamValidator extends AbstractParamValidator {

    private static Log logger = LogFactory.getLog(ResponseParamValidator.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        super.handle(context, chain);

        if (context.getCarryType().equals(MessageCarryType.RESPONSE)) {
            if (context.getTempQueueName() == null || context.getTempQueueName().isEmpty()) {
                logger.error("[handle] for response : the field `tempQueueName` can not be null or empty");
                throw new ParamValidateFailedException("[handle] for response : the field `tempQueueName` " +
                                                           " can not be null or empty");
            }
        }

        chain.handle(context);
    }
}
