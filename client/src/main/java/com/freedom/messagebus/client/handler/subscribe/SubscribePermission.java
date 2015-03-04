package com.freedom.messagebus.client.handler.subscribe;

import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.common.PermissionChecker;
import com.freedom.messagebus.client.message.model.Message;

import java.util.List;

public class SubscribePermission extends PermissionChecker {

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        List<String> subQueueNames = context.getSubQueueNames();
        Message msg = context.getConsumedMsg();

        for (String queueName : subQueueNames) {
            Node subNode = context.getConfigManager().getPubsubNodeMap().get(queueName);

            //has receive permission
            if (commonCheck(context, subNode, false, context.getSourceNode()))
                context.setConsumedMsg(msg);
            else
                context.setConsumedMsg(null);
        }

        chain.handle(context);
    }
}
