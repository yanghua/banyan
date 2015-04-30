package com.messagebus.client.handler.common;

import com.google.common.base.Strings;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.model.Node;

/**
 * Created by yanghua on 3/23/15.
 */
public class MessageBodySizeLimiter extends AbstractHandler {

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        Node targetNode = context.getTargetNode();

        if (targetNode != null && !Strings.isNullOrEmpty(targetNode.getMsgBodySize())) {
            String msgBodySizeStr = targetNode.getMsgBodySize();
            int msgBodySize = Integer.parseInt(msgBodySizeStr);

            if (msgBodySize != -1) {
                Message[] msgs = context.getMessages();
                for (Message msg : msgs) {
                    if (msg.getContent().length > msgBodySize) {
                        throw new RuntimeException("message body's size can not be more than : " + msgBodySizeStr + " B");
                    }
                }
            }
        }

        chain.handle(context);
    }
}
