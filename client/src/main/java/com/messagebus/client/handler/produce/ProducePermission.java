package com.messagebus.client.handler.produce;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.handler.common.PermissionChecker;
import com.messagebus.client.model.Node;
import com.messagebus.common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProducePermission extends PermissionChecker {

    private static final Log logger = LogFactory.getLog(ProducePermission.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        Node sourceNode = context.getSourceNode();
        Node targetNode = context.getTargetNode();

        boolean hasPermission = this.commonCheck(context);
        if (!hasPermission) {
            logger.error("[handle] can not produce message from queue [" + sourceNode.getName() +
                             "] to queue [" + targetNode.getName() + "]");
            throw new RuntimeException("can not produce message from queue [" + sourceNode.getName() +
                                           "] to queue [" + targetNode.getName() + "]");
        }

        String token = context.getToken();

        //send to itself queue
        if (token.equals(context.getSecret())) {
            hasPermission = sourceNode.getNodeId().equals(targetNode.getNodeId());
            hasPermission = hasPermission && sourceNode.getCommunicateType().equals(Constants.COMMUNICATE_TYPE_PRODUCE_CONSUME);
        } else {
            hasPermission = hasPermission && context.getConfigManager().getNodeView(context.getSecret()).getSinkTokens().contains(token);
        }

        if (!hasPermission) {
            logger.error("[handle] can not produce message from queue [" + sourceNode.getName() +
                             "] to queue [" + targetNode.getName() + "]");
            throw new RuntimeException("can not produce message from queue [" + sourceNode.getName() +
                                           "] to queue [" + targetNode.getName() + "]");
        }

        chain.handle(context);
    }
}
