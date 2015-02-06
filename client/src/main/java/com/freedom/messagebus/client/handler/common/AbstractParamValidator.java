package com.freedom.messagebus.client.handler.common;

import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.ParamValidateFailedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

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
    public void handle( MessageContext context,
                        IHandlerChain chain) {
        if (context.getAppId().length() == 0)
            throw new ParamValidateFailedException(" the field : appId of MessageContext can not be empty");

        Node sourceNode = context.getSourceNode();
        if (sourceNode == null) {
            throw new ParamValidateFailedException(" the appId is illegal. ");
        }

        if (sourceNode.getType() == 0) {
            throw new ParamValidateFailedException(" the appId's owner must be a queue node");
        }

        if (!sourceNode.isAvailable()) {
            throw new ParamValidateFailedException(" the appid's owner is not available");
        }
    }
}
