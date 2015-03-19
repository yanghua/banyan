package com.messagebus.client.carry;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.carry.impl.GenericConsumer;
import com.messagebus.client.message.model.Message;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 3/1/15.
 */
public interface IConsumer {

    public void consume(String secret, long timeout, TimeUnit unit, IMessageReceiveListener onMessage);

    public List<Message> consume(String secret, int expectedNum);

}
