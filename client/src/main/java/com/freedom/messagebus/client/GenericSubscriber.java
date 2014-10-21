package com.freedom.messagebus.client;

import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.common.model.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public class GenericSubscriber extends AbstractMessageCarryer implements ISubscriber {

    private static final Log logger = LogFactory.getLog(GenericSubscriber.class);

    public GenericSubscriber(GenericContext context) {
        super(MessageCarryType.SUBSCRIBE, context);
    }

    public ISubscribeManager subscribe(@NotNull List<String> subQueueNames,
                                       @NotNull String receiveQueueName,
                                       @NotNull IMessageReceiveListener receiveListener) throws IOException {
        final MessageContext ctx = new MessageContext();
        ctx.setCarryType(MessageCarryType.SUBSCRIBE);
        ctx.setAppKey(super.context.getAppKey());
        Node node = null;
        if (!receiveQueueName.contains(CONSTS.PUBSUB_QUEUE_NAME_SUFFIX)) {
            String realReceiveQueueName = receiveQueueName + CONSTS.PUBSUB_QUEUE_NAME_SUFFIX;
            node = ConfigManager.getInstance().getPubsubNodeMap().get(realReceiveQueueName);
        } else {
            node = ConfigManager.getInstance().getPubsubNodeMap().get(receiveQueueName);
        }
        ctx.setQueueNode(node);
        ctx.setPool(this.context.getPool());
        ctx.setConnection(this.context.getConnection());
        ctx.setListener(receiveListener);
        ctx.setSync(false);

        this.preProcessSubQueueNames(subQueueNames);
        ctx.setSubQueueNames(subQueueNames);

        carry(ctx);

        return new ISubscribeManager() {
            @Override
            public void close() {
                synchronized (this) {
                    if (ctx.getReceiveEventLoop().isAlive()) {
                        ctx.getReceiveEventLoop().shutdown();
                    }
                }
            }

            @Override
            public void addSubscriber(String subQueueName) {
                synchronized (this) {
                    ctx.getSubQueueNames().add(subQueueName);
                }
            }

            @Override
            public void removeSubscriber(String subQueueName) {
                synchronized (this) {
                    ctx.getSubQueueNames().remove(subQueueName);
                }
            }
        };
    }

    private void preProcessSubQueueNames(@NotNull List<String> subQueueNames) {
        for (int i = 0; i < subQueueNames.size(); i++) {
            String subQueueName = subQueueNames.get(i);
            if (subQueueName.endsWith(CONSTS.PUBSUB_QUEUE_NAME_SUFFIX))
                subQueueNames.set(i, subQueueName.replaceAll(CONSTS.PUBSUB_QUEUE_NAME_SUFFIX, ""));
        }
    }

}
