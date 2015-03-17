package com.messagebus.client.carry;

import com.messagebus.client.IMessageReceiveListener;

import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 3/1/15.
 */
public interface ISubscriber {

    public void subscribe(String secret,
                          IMessageReceiveListener onMessage,
                          long timeout,
                          TimeUnit unit);

}
