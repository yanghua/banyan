package com.messagebus.client.handler.common;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PermissionChecker extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(PermissionChecker.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
    }

    protected boolean commonCheck(MessageContext context) {

        return true;
    }
}
