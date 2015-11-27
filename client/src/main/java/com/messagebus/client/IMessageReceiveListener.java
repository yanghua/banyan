package com.messagebus.client;

import com.messagebus.client.message.model.Message;

/**
 * message receive listener interface
 */
public interface IMessageReceiveListener {

    public void onMessage(Message message);

}
