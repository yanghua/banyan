package com.messagebus.client.handler.consume;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.common.CommonLoopHandler;

/**
 * Created by yanghua on 2/22/15.
 */
class ConsumeLoopHandler extends CommonLoopHandler {

    @Override
    public void process(MessageContext msgContext) {
        IMessageReceiveListener receiveListener = msgContext.getReceiveListener();
        receiveListener.onMessage(msgContext.getConsumedMsg());
    }
}
