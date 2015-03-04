package com.freedom.messagebus.client.carry;

import com.freedom.messagebus.client.IMessageReceiveListener;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 3/1/15.
 */
public interface ISubscriber {

    public void subscribe(IMessageReceiveListener onMessage,
                          List<String> subQueues,
                          long timeout,
                          TimeUnit unit);

}
