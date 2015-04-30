package com.messagebus.client.handler.subscribe;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.handler.common.PermissionChecker;
import com.messagebus.client.model.Node;
import com.messagebus.common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SubscribePermission extends PermissionChecker {

    private static final Log logger = LogFactory.getLog(SubscribePermission.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        Node sourceNode = context.getSourceNode();

        if (!super.commonCheck(context)) {
            logger.error("permission error : can not consume ");
            throw new RuntimeException("permission error : can not consume ");
        }

        boolean hasPermission = sourceNode.getCommunicateType().equals(Constants.COMMUNICATE_TYPE_SUBSCRIBE)
            || sourceNode.getCommunicateType().equals(Constants.COMMUNICATE_TYPE_PUBLISH_SUBSCRIBE);

        if (!hasPermission) {
            logger.error("permission error : can not subscribe ");
            throw new RuntimeException("permission error : can not subscribe ");
        }

        chain.handle(context);
    }
}
