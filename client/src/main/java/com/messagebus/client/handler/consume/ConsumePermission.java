package com.messagebus.client.handler.consume;

import com.google.common.base.Strings;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.handler.common.PermissionChecker;
import com.messagebus.client.model.Node;
import com.messagebus.common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConsumePermission extends PermissionChecker {

    private static final Log logger = LogFactory.getLog(ConsumePermission.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        if (!super.commonCheck(context)) {
            logger.error("permission error : can not consume ");
            throw new RuntimeException("permission error : can not consume ");
        }

        Node sourceNode = context.getSourceNode();
        boolean hasPermission = !Strings.isNullOrEmpty(sourceNode.getCommunicateType());
        hasPermission = hasPermission && !sourceNode.getCommunicateType().equals(Constants.COMMUNICATE_TYPE_PRODUCE);
        hasPermission = hasPermission || sourceNode.isInner();

        if (!hasPermission) {
            logger.error("permission error : can not consume ");
            throw new RuntimeException("permission error : can not consume ");
        }

        chain.handle(context);
    }
}
