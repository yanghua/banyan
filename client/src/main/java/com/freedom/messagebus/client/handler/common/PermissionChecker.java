package com.freedom.messagebus.client.handler.common;

import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.core.config.ConfigManager;
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

    protected boolean commonCheck(Node source, Node target, boolean isSend) {
        //inner channel just let it go!
        if (source.isInner() || target.isInner())
            return true;

        Map<String, byte[]> permissionQueryArrMap = null;

        if (isSend) {
            permissionQueryArrMap = ConfigManager.getInstance().getSendPermByteQueryArrMap();
        } else {
            permissionQueryArrMap = ConfigManager.getInstance().getReceivePermByteQueryArrMap();
        }

        byte[] grantIdSwitchArr = permissionQueryArrMap.get(String.valueOf(source.getNodeId()));

        return grantIdSwitchArr != null && grantIdSwitchArr[target.getNodeId()] == 1;
    }
}
