package com.freedom.messagebus.client;

import com.freedom.messagebus.common.IMessageReceiveListener;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * the interface of consumer
 */
public interface IConsumer {

    /**
     * consume message
     *
     * @param queueName       the name of queue that the consumer want to connect
     *                        generally, is the app-name
     * @param receiveListener the message receiver
     * @return a consumer's closer used to let the app control the consumer
     * (actually, the message receiver is needed to be controlled)
     * @throws IOException
     */
    @NotNull
    public IConsumerCloser consume(@NotNull String queueName,
                                   @NotNull IMessageReceiveListener receiveListener) throws IOException;

}
