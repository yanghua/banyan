package com.messagebus.client;

import com.messagebus.client.message.model.IMessage;

/**
 * Created by yanghua on 3/16/15.
 */
public interface IRequestListener {

    public IMessage onRequest(IMessage requestMsg);

}
