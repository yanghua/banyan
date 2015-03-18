package com.messagebus.client.handler.subscribe;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.common.CommonLoopHandler;
import com.messagebus.client.message.model.IMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by yanghua on 2/22/15.
 */
public class SubscribeLoopHandler extends CommonLoopHandler {

    private static final Log logger = LogFactory.getLog(SubscribeLoopHandler.class);

    @Override
    public void process(MessageContext msgContext) {
        IMessage msg = msgContext.getConsumedMsg();
        IMessageReceiveListener receiveListener = msgContext.getReceiveListener();
        receiveListener.onMessage(msg);
    }

}
