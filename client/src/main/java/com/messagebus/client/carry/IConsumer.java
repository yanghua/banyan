package com.messagebus.client.carry;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.message.model.IMessage;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 3/1/15.
 */
public interface IConsumer {

    public void consume(String secret, long timeout, TimeUnit unit, IMessageReceiveListener onMessage);

    public List<IMessage> consume(String secret, int expectedNum);

}
