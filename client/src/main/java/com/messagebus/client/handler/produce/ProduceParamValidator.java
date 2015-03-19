package com.messagebus.client.handler.produce;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.handler.common.AbstractParamValidator;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

public class ProduceParamValidator extends AbstractParamValidator {

    private static final Log logger = LogFactory.getLog(ProduceParamValidator.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        super.handle(context, chain);

        if (!context.getCarryType().equals(MessageCarryType.PRODUCE)) {
            throw new RuntimeException("message carry type should be produce ");
        }

        if (context.getTargetNode() == null) {
            throw new RuntimeException("target node can not be null ");
        }

        if (context.getTargetNode().getType().equals("0")) {
            throw new RuntimeException("target node's type is illegal");
        }

        this.validateMessagesProperties(context);

        chain.handle(context);
    }

    private void validateMessagesProperties(MessageContext context) {
        Date currentDate = new Date();
        for (Message msg : context.getMessages()) {
            //app id
            if (msg.getAppId() == null || msg.getAppId().isEmpty())
                msg.setAppId(context.getSourceNode().getAppId());

            //timestamp
            if (msg.getTimestamp() == null)
                msg.setTimestamp(currentDate);

            if (!MessageType.QueueMessage.getType().equals(msg.getType())) {
                throw new RuntimeException("the message is not QueueMessage");
            }
        }
    }

}
