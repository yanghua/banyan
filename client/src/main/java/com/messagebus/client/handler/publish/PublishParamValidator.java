package com.messagebus.client.handler.publish;

import com.google.common.base.Strings;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.handler.common.AbstractParamValidator;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.model.MessageCarryType;
import com.messagebus.client.model.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

public class PublishParamValidator extends AbstractParamValidator {

    private static Log logger = LogFactory.getLog(PublishParamValidator.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        super.handle(context, chain);

        if (!context.getCarryType().equals(MessageCarryType.PUBLISH)) {
            throw new RuntimeException("message carry type should be publish ");
        }

        this.validateMessageProperties(context);

        chain.handle(context);
    }

    private void validateMessageProperties(MessageContext context) {
        Date currentDate = new Date();
        Node sourceNode = context.getSourceNode();
        for (Message msg : context.getMessages()) {
            //app id
            if (Strings.isNullOrEmpty(msg.getAppId())) {
                msg.setAppId(sourceNode.getAppId());
            }

            if (Strings.isNullOrEmpty(msg.getReplyTo())) {
                msg.setReplyTo(context.getSourceNode().getName());
            }

            //timestamp
            if (msg.getTimestamp() == 0)
                msg.setTimestamp(currentDate.getTime());
        }
    }
}
