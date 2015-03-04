package com.freedom.messagebus.client.handler.consume;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.common.PermissionChecker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConsumePermission extends PermissionChecker {

    private static final Log logger = LogFactory.getLog(ConsumePermission.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        chain.handle(context);
    }
}
