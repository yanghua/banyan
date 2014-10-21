package com.freedom.messagebus.client.handler.subscribe;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.common.message.MessageType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MessageFilter extends AbstractHandler {

    @Override
    public void handle(@NotNull MessageContext context, @NotNull IHandlerChain chain) {
        List<String> subQueueNames = context.getSubQueueNames();
        Message msg = context.getConsumedMsg();

        //if broadcast msg then let it go
        if (msg.getMessageType().equals(MessageType.BroadcastMessage))
            chain.handle(context);
        else if (msg.getMessageType().equals(MessageType.PubSubMessage)) {
            boolean matched = filterMessage(msg.getMessageHeader().getReplyTo(), subQueueNames);
            if (!matched)
                context.setConsumedMsg(null);

            chain.handle(context);
        } else {
            context.setConsumedMsg(null);
            chain.handle(context);
        }

    }

    private boolean filterMessage(@NotNull String replyTo, @NotNull List<String> subQueueNames) {
        for (String queueName : subQueueNames) {
            if (queueName.equals(replyTo))
                return true;
        }

        return false;
    }
}
