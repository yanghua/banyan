package com.freedom.messagebus.client.handler.common;

import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

public class PermissionChecker extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(PermissionChecker.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
    }

    protected boolean commonCheck(MessageContext context, Node target, boolean isSend, Node source) {
        //inner channel just let it go!
        if (source.isInner() || target.isInner())
            return true;

        Map<String, byte[]> permissionQueryArrMap = null;

        if (isSend) {
            permissionQueryArrMap = context.getConfigManager().getSendPermByteQueryArrMap();
        } else {
            permissionQueryArrMap = context.getConfigManager().getReceivePermByteQueryArrMap();
        }

        byte[] grantIdSwitchArr = permissionQueryArrMap.get(String.valueOf(source.getNodeId()));

        return grantIdSwitchArr != null && grantIdSwitchArr[target.getNodeId()] == 1;
    }
}
