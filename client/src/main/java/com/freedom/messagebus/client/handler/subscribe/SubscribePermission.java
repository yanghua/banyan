package com.freedom.messagebus.client.handler.subscribe;

import com.freedom.messagebus.business.message.model.Message;
import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.common.PermissionChecker;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SubscribePermission extends PermissionChecker {

    @Override
    public void handle(@NotNull MessageContext context, @NotNull IHandlerChain chain) {
        List<String> subQueueNames = context.getSubQueueNames();
        Message msg = context.getConsumedMsg();

        ConfigManager configManager = ConfigManager.getInstance();
        for (String queueName : subQueueNames) {
            Node targetNode = configManager.getPubsubNodeMap().get(queueName);

            //has receive permission
            if (commonCheck(context.getSourceNode(), targetNode, false))
                context.setConsumedMsg(msg);
            else
                context.setConsumedMsg(null);
        }

        chain.handle(context);
    }
}
