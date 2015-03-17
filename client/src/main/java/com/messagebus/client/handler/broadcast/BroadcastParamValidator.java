package com.messagebus.client.handler.broadcast;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.handler.common.AbstractParamValidator;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

public class BroadcastParamValidator extends AbstractParamValidator {

    private static final Log logger = LogFactory.getLog(BroadcastParamValidator.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        super.handle(context, chain);

        if (!context.getCarryType().equals(MessageCarryType.BROADCAST)) {
            throw new RuntimeException("the message carry type should be broadcast");
        }


        this.validateMessageProperties(context);

        chain.handle(context);
    }

    private void validateMessageProperties(MessageContext context) {
        Date currentDate = new Date();
        for (Message msg : context.getMessages()) {
            //app id
            if (msg.getMessageHeader().getAppId() == null || msg.getMessageHeader().getAppId().isEmpty())
                msg.getMessageHeader().setAppId(context.getSourceNode().getAppId());

            //timestamp
            if (msg.getMessageHeader().getTimestamp() == null)
                msg.getMessageHeader().setTimestamp(currentDate);

            if (!MessageType.BroadcastMessage.getType().equals(msg.getMessageHeader().getType())) {
                logger.error("[validateMessagesProperites] the message's type is not  BroadcastMessage ");
                throw new RuntimeException(" the message's type is not  BroadcastMessage ");
            }
        }
    }
}
