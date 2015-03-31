package com.messagebus.client.handler.broadcast;

import com.messagebus.business.model.Channel;
import com.messagebus.business.model.Node;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.handler.common.PermissionChecker;

/**
 * Created by yanghua on 3/17/15.
 */
public class BroadcastPermission extends PermissionChecker {

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        boolean hasPermission = this.commonCheck(context);

        if (!hasPermission) {
            throw new RuntimeException("can not broadcast message ");
        }

        Node sourceNode = context.getSourceNode();
        hasPermission = sourceNode.isCanBroadcast();

        if (!hasPermission) {
            throw new RuntimeException("permission error!");
        }

        chain.handle(context);
    }
}
