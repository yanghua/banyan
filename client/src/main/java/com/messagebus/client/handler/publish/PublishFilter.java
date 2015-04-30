package com.messagebus.client.handler.publish;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.model.Node;

import java.util.List;

/**
 * Created by yanghua on 3/17/15.
 */
public class PublishFilter extends AbstractHandler {

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        List<Node> pushToNodes = context.getConfigManager().getNodeView(context.getSecret()).getSubscribeNodes();
        if (pushToNodes == null || pushToNodes.size() == 0) return;

        context.getOtherParams().put("publishList", pushToNodes);

        chain.handle(context);
    }
}
