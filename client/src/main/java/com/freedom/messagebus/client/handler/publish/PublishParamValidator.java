package com.freedom.messagebus.client.handler.publish;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.common.AbstractParamValidator;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.model.MessageType;
import com.google.common.base.Strings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

public class PublishParamValidator extends AbstractParamValidator {

    private static Log logger = LogFactory.getLog(PublishParamValidator.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        super.handle(context, chain);

        this.validateMessageProperties(context);

        chain.handle(context);
    }

    private void validateMessageProperties(MessageContext context) {
        Date currentDate = new Date();
        for (Message msg : context.getMessages()) {
            //app id
            if (Strings.isNullOrEmpty(msg.getMessageHeader().getAppId())) {
                msg.getMessageHeader().setAppId(context.getAppId());
            }

            if (Strings.isNullOrEmpty(msg.getMessageHeader().getReplyTo())) {
                msg.getMessageHeader().setReplyTo(context.getSourceNode().getName());
            }

            //timestamp
            if (msg.getMessageHeader().getTimestamp() == null)
                msg.getMessageHeader().setTimestamp(currentDate);

            if (!MessageType.PubSubMessage.getType().equals(msg.getMessageHeader().getType())) {
                logger.error("[validateMessagesProperites] the message is not a  PubSubMessage. ");
                throw new RuntimeException("the message is not a  PubSubMessage");
            }
        }
    }
}
