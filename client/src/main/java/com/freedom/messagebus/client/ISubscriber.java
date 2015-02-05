package com.freedom.messagebus.client;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public interface ISubscriber extends IBasicOperator {

    public ISubscribeManager subscribe(@NotNull List<String> subQueueNames,
                                       @NotNull IMessageReceiveListener receiveListener) throws IOException;

}
