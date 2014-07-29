package com.freedom.messagebus.client;

import com.freedom.messagebus.client.core.message.Message;
import com.freedom.messagebus.client.model.MessageFormat;

/**
 * the interface of message receive listener
 */
public interface IMessageReceiveListener {

    /**
     * the callback method when received message
     *
     * @param msg    received message
     * @param format message format
     */
    public void onMessage(Message msg, MessageFormat format);

}
