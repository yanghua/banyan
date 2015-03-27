package com.messagebus.client.handler.publish;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.messagebus.business.model.Channel;
import com.messagebus.business.model.Node;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by yanghua on 3/17/15.
 */
public class PublishFilter extends AbstractHandler {

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        Node sourceNode = context.getSourceNode();
        String pushToIds = context.getConfigManager().getPubsubChannelMap().get(sourceNode.getNodeId());

        if (Strings.isNullOrEmpty(pushToIds)) {
            return;
        }

        Iterator<String> pushToIdIterator = Splitter.on(',').split(pushToIds).iterator();

        List<Node> pushToNodes = new ArrayList<>();

        while (pushToIdIterator.hasNext()) {
            String pushToId = pushToIdIterator.next();
            Node pushToNode = context.getConfigManager().getIdNodeMap().get(pushToId);
            if (pushToNode == null) continue;
            if (pushToNode.getType().equals("0")) continue;
            if (!pushToNode.isAvailable()) continue;
            String nodeName = pushToNode.getName();
            if (!context.getConfigManager().getPubsubNodeMap().containsKey(nodeName)) continue;

            pushToNodes.add(pushToNode);
        }

        context.getOtherParams().put("publishList", pushToNodes);

        chain.handle(context);
    }
}
