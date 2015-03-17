package com.messagebus.client;

import com.messagebus.client.message.model.Message;

/**
 * Created by yanghua on 3/16/15.
 */
public interface IRequestListener {

    public Message onRequest(Message requestMsg);

}
