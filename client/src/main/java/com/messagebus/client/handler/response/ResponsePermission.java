package com.messagebus.client.handler.response;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.handler.common.PermissionChecker;
import com.messagebus.client.model.Node;
import com.messagebus.common.Constants;

/**
 * Created by yanghua on 3/17/15.
 */
public class ResponsePermission extends PermissionChecker {

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        if (!super.commonCheck(context)) {
            throw new RuntimeException("permission error : can not consume ");
        }

        Node sourceNode = context.getSourceNode();
        boolean hasPermission = !sourceNode.getCommunicateType().equals(Constants.COMMUNICATE_TYPE_REQUEST);

        if (!hasPermission) {
            throw new RuntimeException("permission error : can not consume ");
        }

        chain.handle(context);
    }
}
