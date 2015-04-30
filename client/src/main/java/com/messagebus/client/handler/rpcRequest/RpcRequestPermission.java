package com.messagebus.client.handler.rpcRequest;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.handler.common.PermissionChecker;
import com.messagebus.client.model.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by yanghua on 4/9/15.
 */
public class RpcRequestPermission extends PermissionChecker {

    private static final Log logger = LogFactory.getLog(RpcRequestPermission.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        Node sourceNode = context.getSourceNode();
        Node targetNode = context.getTargetNode();
        boolean hasPermission = this.commonCheck(context);

        if (!hasPermission) {
            logger.error("[handle] can not request message from queue : " + sourceNode.getName() +
                             " to queue : " + targetNode.getName());
            throw new RuntimeException("can not request message from queue : " + sourceNode.getName() +
                                           " to queue : " + targetNode.getName());
        }

        String token = context.getToken();

        hasPermission = hasPermission
            && context.getConfigManager().getNodeView(context.getSecret()).getSinkTokens().contains(token);

        if (!hasPermission) {
            logger.error("[handle] can not produce message from queue [" + sourceNode.getName() +
                             "] to queue [" + targetNode.getName() + "]");
            throw new RuntimeException("can not produce message from queue [" + sourceNode.getName() +
                                           "] to queue [" + targetNode.getName() + "]");
        }

        chain.handle(context);
    }
}
