package com.messagebus.client;

import com.messagebus.client.message.model.IMessage;

/**
 * message receive listener interface
 */
public interface IMessageReceiveListener {

    public void onMessage(IMessage message);

}
