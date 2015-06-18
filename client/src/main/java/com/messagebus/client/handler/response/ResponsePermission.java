package com.messagebus.client.handler.response;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.handler.common.PermissionChecker;
import com.messagebus.client.model.Node;
import com.messagebus.common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * response permission
 */
public class ResponsePermission extends PermissionChecker {

    private static final Log logger = LogFactory.getLog(ResponsePermission.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        if (!super.commonCheck(context)) {
            throw new RuntimeException("permission error : can not response ");
        }

        Node sourceNode = context.getSourceNode();
        boolean hasPermission = sourceNode.getCommunicateType().equals(Constants.COMMUNICATE_TYPE_RESPONSE)
            || sourceNode.getCommunicateType().equals(Constants.COMMUNICATE_TYPE_REQUEST_RESPONSE);

        if (!hasPermission) {
            logger.error("permission error : can not response. " +
                             "may be communicate type is wrong. " +
                             "current secret is : " + sourceNode.getSecret());
            throw new RuntimeException("permission error : can not response. " +
                                           "may be communicate type is wrong. " +
                                           "current secret is : " + sourceNode.getSecret());
        }

        chain.handle(context);
    }
}
