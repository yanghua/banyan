package com.messagebus.client.handler.subscribe;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.common.CommonLoopHandler;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by yanghua on 2/22/15.
 */
public class SubscribeLoopHandler extends CommonLoopHandler {

    private static final Log logger = LogFactory.getLog(SubscribeLoopHandler.class);

    @Override
    public void process(MessageContext msgContext) {
        Message msg = msgContext.getConsumedMsg();

        if (msg.getMessageType().equals(MessageType.PubSubMessage)) {
//            String nodeName = msg.getMessageHeader().getReplyTo();
//            if (Strings.isNullOrEmpty(nodeName)) {
//                logger.error("received message has not property : replyTo");
//                return;
//            }
//            Node publishNode = msgContext.getConfigManager().getPubsubNodeMap().get(nodeName);
//            boolean matched = filterMessage(publishNode.getNodeId(), publisherListStr);
//            if (!matched) return;

            IMessageReceiveListener receiveListener = msgContext.getReceiveListener();
            receiveListener.onMessage(msg);
        }
    }

//    private boolean filterMessage(String replyToId, String publisherListStr) {
//        if (Strings.isNullOrEmpty(replyToId) || Strings.isNullOrEmpty(publisherListStr)) {
//            return false;
//        }
//
//        return publisherListStr.contains(replyToId) || publisherListStr.contains(replyToId + ",");
//    }

}
