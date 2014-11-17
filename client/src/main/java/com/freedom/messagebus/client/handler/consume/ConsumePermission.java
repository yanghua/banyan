package com.freedom.messagebus.client.handler.consume;

import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.PermissionException;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.common.PermissionChecker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

public class ConsumePermission extends PermissionChecker {

    private static final Log logger = LogFactory.getLog(ConsumePermission.class);

    @Override
    public void handle(@NotNull MessageContext context, @NotNull IHandlerChain chain) {
        Node sourceNode = context.getSourceNode();
        Node targetNode = context.getTargetNode();

        boolean hasPermission = this.commonCheck(sourceNode, targetNode, false);
        if (!hasPermission) {
            logger.error("[handle] can not consume message from queue : " + targetNode.getName() +
                             " with queue : " + sourceNode.getName());
            throw new PermissionException("can not consume message from queue : " + targetNode.getName() +
                                              " with queue : " + sourceNode.getName());
        }

        chain.handle(context);
    }
}
