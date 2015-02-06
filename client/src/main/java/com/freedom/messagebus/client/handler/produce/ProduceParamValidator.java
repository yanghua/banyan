package com.freedom.messagebus.client.handler.produce;

import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.model.MessageType;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.ParamValidateFailedException;
import com.freedom.messagebus.client.handler.common.AbstractParamValidator;
import com.freedom.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Date;

public class ProduceParamValidator extends AbstractParamValidator {

    private static final Log logger = LogFactory.getLog(ProduceParamValidator.class);

    @Override
    public void handle( MessageContext context,  IHandlerChain chain) {
        super.handle(context, chain);

        if (context.getCarryType().equals(MessageCarryType.PRODUCE)) {
            if (context.getTargetNode().getRoutingKey() == null || context.getTargetNode().getRoutingKey().isEmpty()) {
                logger.error("[handle] the context field : queueNode is illegal. the routingkey can not be null or empty");
                throw new ParamValidateFailedException("[handle] the context field : queueNode is illegal. " +
                                                           "the routingkey can not be null or empty");
            }

            if (context.getTargetNode() == null) {
                logger.error("[handle] the context field : queueNode is illegal. it can not be null");
                throw new ParamValidateFailedException("the context field : queueNode is illegal. " +
                                                           "it can not be null ");
            }

            if (context.getTargetNode().getType() == 0) {
                logger.error("[handle] the context field : queueNode is illegal. it must be a queue type");
                throw new ParamValidateFailedException("[handle] the context field : queueNode is illegal. " +
                                                           "it must be a queue type");
            }

            if (context.getTargetNode().getName() == null || context.getTargetNode().getName().isEmpty()) {
                logger.error("[handle] the context field : queueNode is illegal. the name can not be null or empty");
                throw new ParamValidateFailedException("[handle] the context field : queueNode is illegal. " +
                                                           "the name can not be null or empty");
            }

            this.validateMessagesProperties(context);
        }

        chain.handle(context);
    }

    private void validateMessagesProperties( MessageContext context) {
        Date currentDate = new Date();
        for (Message msg : context.getMessages()) {
            //app id
            if (msg.getMessageHeader().getAppId() == null || msg.getMessageHeader().getAppId().isEmpty())
                msg.getMessageHeader().setAppId(context.getAppId());

            //timestamp
            if (msg.getMessageHeader().getTimestamp() == null)
                msg.getMessageHeader().setTimestamp(currentDate);

            if (!MessageType.QueueMessage.getType().equals(msg.getMessageHeader().getType())) {
                logger.error("[validateMessagesProperites] the message is not  `QueueMessage`. ");
            }
        }
    }

}
