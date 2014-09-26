package com.freedom.messagebus.client;

import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.IMessageReceiveListener;
import com.freedom.messagebus.common.model.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * a generic consumer which implements IConsumer
 */
class GenericConsumer extends AbstractMessageCarryer implements IConsumer {

    private static final Log logger = LogFactory.getLog(GenericConsumer.class);

    public GenericConsumer(GenericContext context) {
        super(MessageCarryType.CONSUME, context);
    }

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
    @Override
    public IConsumerCloser consume(@NotNull String queueName,
                                   @NotNull IMessageReceiveListener receiveListener) throws IOException {
        final MessageContext context = new MessageContext();
        context.setCarryType(MessageCarryType.CONSUME);
        context.setAppKey(super.context.getAppKey());
        Node node = ConfigManager.getInstance().getQueueNodeMap().get(queueName);
        context.setQueueNode(node);

        context.setPool(this.context.getPool());
        context.setConnection(this.context.getConnection());
        context.setListener(receiveListener);

        //launch
        carry(context);

        return new IConsumerCloser() {
            @Override
            public void closeConsumer() {
                context.getReceiver().shutdown();
            }
        };
    }


}
