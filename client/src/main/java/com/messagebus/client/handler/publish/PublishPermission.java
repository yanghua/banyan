package com.messagebus.client.handler.publish;

import com.messagebus.business.model.Node;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.handler.common.PermissionChecker;

/**
 * Created by yanghua on 3/17/15.
 */
public class PublishPermission extends PermissionChecker {

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        Node sourceNode = context.getSourceNode();
        boolean hasPermission = this.commonCheck(context);
        if (!hasPermission) {
            throw new RuntimeException("can not publish message ");
        }

        hasPermission = context.getConfigManager().getPubsubNodeMap().containsKey(sourceNode.getName());

        if (!hasPermission) {
            throw new RuntimeException("can not publish message ");
        }

        chain.handle(context);
    }
}
