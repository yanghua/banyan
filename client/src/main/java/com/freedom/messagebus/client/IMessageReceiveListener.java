package com.freedom.messagebus.client;

import com.freedom.messagebus.common.message.Message;

/**
 * message receive listener interface
 */
public interface IMessageReceiveListener {

    public void onMessage(Message message, IReceiverCloser consumerCloser);

}
