package com.messagebus.client.handler.produce;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.handler.common.AbstractParamValidator;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.client.model.MessageCarryType;
import com.messagebus.client.model.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Date;

public class ProduceParamValidator extends AbstractParamValidator {

    private static final Log logger = LogFactory.getLog(ProduceParamValidator.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        super.handle(context, chain);

        Node currentNode = context.getSourceNode();

        if (!context.getCarryType().equals(MessageCarryType.PRODUCE)) {
            throw new RuntimeException("message carry type should be produce, but current is : "
                                           + context.getCarryType()
                                           + "; node name : " + currentNode.getName()
                                           + "; node secret is : " + currentNode.getSecret());
        }

        if (context.getTargetNode() == null) {
            throw new RuntimeException("target node can not be null "
                                           + "; node name : " + currentNode.getName()
                                           + "; node secret is : " + currentNode.getSecret());
        }

        if (context.getTargetNode().getType().equals("0")) {
            throw new RuntimeException("target node's type is illegal , "
                                           + "target node name : " + context.getTargetNode().getName());
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
                throw new RuntimeException("the message is not QueueMessage");
            }
        }
    }

}
