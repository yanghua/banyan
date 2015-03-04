package com.freedom.messagebus.client.carry;

import com.freedom.messagebus.client.IMessageReceiveListener;
import com.freedom.messagebus.client.message.model.Message;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 3/1/15.
 */
public interface IConsumer {

    public void consume(IMessageReceiveListener onMessage, long timeout, TimeUnit unit);

    public List<Message> consume(int expectedNum);

}
