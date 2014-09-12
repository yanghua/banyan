package com.freedom.messagebus.client;

import com.freedom.messagebus.client.model.MessageCarryType;
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
     * @param appKey          the app key which the consumer representation
     * @param msgType         message type (business / system)
     * @param queueName       the name of queue that the consumer want to connect
     *                        generally, is the app-name
     * @param receiveListener the message receiver
     * @return a consumer's closer used to let the app control the consumer
     * (actually, the message receiver is needed to be controlled)
     * @throws IOException
     */
    @NotNull
    @Override
    public IConsumerCloser consume(@NotNull String appKey, @NotNull String msgType, @NotNull String queueName,
                                   @NotNull IMessageReceiveListener receiveListener) throws IOException {
        final MessageContext context = new MessageContext();
        context.setCarryType(MessageCarryType.CONSUME);
        context.setAppKey(appKey);
        context.setMsgType(msgType);
        context.setRuleValue(queueName);
        context.setListener(receiveListener);

        context.setZooKeeper(this.context.getZooKeeper());
        context.setPool(this.context.getPool());
        context.setConfigManager(this.context.getConfigManager());
        context.setConnection(this.context.getConnection());

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
