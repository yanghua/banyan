package com.freedom.messagebus.client;

import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.common.model.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

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
    public IReceiveCloser consume(@NotNull String queueName,
                                   @NotNull IMessageReceiveListener receiveListener) throws IOException {
        final MessageContext ctx = new MessageContext();
        ctx.setCarryType(MessageCarryType.CONSUME);
        ctx.setAppKey(super.context.getAppKey());
        Node node = ConfigManager.getInstance().getQueueNodeMap().get(queueName);
        ctx.setQueueNode(node);

        ctx.setPool(this.context.getPool());
        ctx.setConnection(this.context.getConnection());
        ctx.setListener(receiveListener);
        ctx.setSync(false);

        //launch
        carry(ctx);

        return new IReceiveCloser() {
            @Override
            public void close() {
                synchronized (this) {
                    if (ctx.getReceiveEventLoop().isAlive()) {
                        ctx.getReceiveEventLoop().shutdown();
                    }
                }
            }
        };
    }

    /**
     * consume with sync-mode, when received messages' num equal the given num
     * or timeout the consume will return
     *
     * @param queueName the name of queue that the consumer want to connect
     * @param num       the num which the client expected (the result's num may not be equals to the given num)
     * @return received message
     */
    @NotNull
    @Override
    public List<Message> consume(@NotNull String queueName, int num) {
        final MessageContext ctx = new MessageContext();
        ctx.setCarryType(MessageCarryType.CONSUME);
        ctx.setAppKey(super.context.getAppKey());
        Node node = ConfigManager.getInstance().getQueueNodeMap().get(queueName);
        ctx.setQueueNode(node);
        ctx.setPool(this.context.getPool());
        ctx.setConnection(this.context.getConnection());
        ctx.setConsumeMsgNum(num);
        ctx.setSync(true);

        carry(ctx);

        return ctx.getConsumeMsgs();
    }
}
