package com.messagebus.client.handler.common;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.model.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * parameter validate handler
 */
public abstract class AbstractParamValidator extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(AbstractParamValidator.class);

    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    @Override
    public void handle(MessageContext context,
                       IHandlerChain chain) {
        Node sourceNode = context.getSourceNode();
        if (sourceNode == null) {
            throw new RuntimeException(" the source node is illegal. ");
        }

        if (sourceNode.getType().equals("0")) {
            throw new RuntimeException(" the node with name " + sourceNode.getName() + " must be a queue ");
        }

        if (!sourceNode.isAvailable()) {
            throw new RuntimeException(" the queue " + sourceNode.getName() + " is not available ");
        }
    }
}
