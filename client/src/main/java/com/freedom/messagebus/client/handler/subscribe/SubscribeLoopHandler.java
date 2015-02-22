package com.freedom.messagebus.client.handler.subscribe;

import com.freedom.messagebus.client.IMessageReceiveListener;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.common.CommonLoopHandler;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.model.MessageType;

import java.util.List;

/**
 * Created by yanghua on 2/22/15.
 */
public class SubscribeLoopHandler extends CommonLoopHandler {

    @Override
    public void process(MessageContext msgContext) {
        List<String> subQueueNames = msgContext.getSubQueueNames();
        Message msg = msgContext.getConsumedMsg();

        if (msg.getMessageType().equals(MessageType.PubSubMessage)) {
            boolean matched = filterMessage(msg.getMessageHeader().getReplyTo(), subQueueNames);
            if (!matched)
                msgContext.setConsumedMsg(null);

            IMessageReceiveListener receiveListener = msgContext.getListener();
            receiveListener.onMessage(msgContext.getConsumedMsg());
        }
    }

    private boolean filterMessage(String replyTo, List<String> subQueueNames) {
        for (String queueName : subQueueNames) {
            if (queueName.equals(replyTo))
                return true;
        }

        return false;
    }

}
