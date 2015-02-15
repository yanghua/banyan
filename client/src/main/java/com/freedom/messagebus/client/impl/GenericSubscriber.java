package com.freedom.messagebus.client.impl;

import com.freedom.messagebus.client.*;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.List;

public class GenericSubscriber extends AbstractMessageCarryer implements ISubscriber {

    private static final Log logger = LogFactory.getLog(GenericSubscriber.class);

    public GenericSubscriber() {
        super(MessageCarryType.SUBSCRIBE);
    }

    public ISubscribeManager subscribe(List<String> subQueueNames,
                                       IMessageReceiveListener receiveListener) throws IOException {
        final MessageContext ctx = new MessageContext();
        ctx.setCarryType(MessageCarryType.SUBSCRIBE);
        ctx.setAppId(this.context.getAppId());
        ctx.setSourceNode(ConfigManager.getInstance().getAppIdQueueMap().get(this.context.getAppId()));

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

    private void preProcessSubQueueNames(List<String> subQueueNames) {
        for (int i = 0; i < subQueueNames.size(); i++) {
            String subQueueName = subQueueNames.get(i);
            if (subQueueName.endsWith(Constants.PUBSUB_QUEUE_NAME_SUFFIX))
                subQueueNames.set(i, subQueueName.replaceAll(Constants.PUBSUB_QUEUE_NAME_SUFFIX, ""));
        }
    }

}
