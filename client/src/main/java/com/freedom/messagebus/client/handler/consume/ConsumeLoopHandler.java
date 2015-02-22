package com.freedom.messagebus.client.handler.consume;

import com.freedom.messagebus.client.IMessageReceiveListener;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.common.CommonLoopHandler;

/**
 * Created by yanghua on 2/22/15.
 */
class ConsumeLoopHandler extends CommonLoopHandler {

    @Override
    public void process(MessageContext msgContext) {
        IMessageReceiveListener receiveListener = msgContext.getListener();
        receiveListener.onMessage(msgContext.getConsumedMsg());
    }
}
