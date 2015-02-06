package com.freedom.messagebus.client;

import com.freedom.messagebus.client.message.model.Message;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

/**
 * the interface of consumer
 */
public interface IConsumer extends IBasicOperator {

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

    public IReceiverCloser consume( String queueName,
                                    IMessageReceiveListener receiveListener) throws IOException;

    /**
     * consume with sync-mode, when received messages' num equal the given num
     * or timeout the consume will return
     *
     * @param queueName the name of queue that the consumer want to connect
     * @param num       the num which the client expected (the result's num may not be equals to the given num)
     * @return received message
     */

    public List<Message> consume( String queueName, int num);

}
