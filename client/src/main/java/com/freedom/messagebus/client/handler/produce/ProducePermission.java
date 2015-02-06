package com.freedom.messagebus.client.handler.produce;

import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.PermissionException;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.common.PermissionChecker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProducePermission extends PermissionChecker {

    private static final Log logger = LogFactory.getLog(ProducePermission.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        Node sourceNode = context.getSourceNode();
        Node targetNode = context.getTargetNode();

        boolean hasPermission = this.commonCheck(sourceNode, targetNode, true);
        if (!hasPermission) {
            logger.error("[handle] can not produce message from queue [" + sourceNode.getName() +
                             "] to queue [" + targetNode.getName() + "]");
            throw new PermissionException("can not produce message from queue [" + sourceNode.getName() +
                                              "] to queue [" + targetNode.getName() + "]");
        }

        chain.handle(context);
    }
}
