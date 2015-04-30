package com.messagebus.client.handler.publish;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.handler.common.PermissionChecker;
import com.messagebus.client.model.Node;

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

        hasPermission = sourceNode.getCommunicateType().equals("publish")
            || sourceNode.getCommunicateType().equals("publish-subscribe");

        if (!hasPermission) {
            throw new RuntimeException("can not publish message ");
        }

        chain.handle(context);
    }
}
