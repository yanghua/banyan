package com.messagebus.client.handler.request;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.handler.ParamValidateFailedException;
import com.messagebus.client.handler.common.AbstractParamValidator;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

public class RequestParamValidator extends AbstractParamValidator {

    private static Log logger = LogFactory.getLog(RequestParamValidator.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        super.handle(context, chain);

        if (!context.getCarryType().equals(MessageCarryType.REQUEST)) {
            throw new RuntimeException("message carry type should be request");
        }

        Message[] msgs = context.getMessages();
        if (msgs == null || msgs.length != 1) {
            throw new RuntimeException("illegal message array length : in request-response mode : " +
                                           "just send a request message once a time ");
        }

        if (context.getTargetNode() == null) {
            throw new RuntimeException("the context field : queueNode is illegal. " +
                                           "it can not be null ");
        }

        if (context.getTargetNode().getType().equals("0")) {
            throw new ParamValidateFailedException("[handle] the context field : queueNode is illegal. " +
                                                       "it must be a queue type");
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
            if (msg.getTimestamp() == 0)
                msg.setTimestamp(currentDate.getTime());

            if (!MessageType.QueueMessage.getType().equals(msg.getType())) {
                logger.error("[validateMessagesProperites] there is a message is not  `QueueMessage`. ");
            }
        }
    }
}
