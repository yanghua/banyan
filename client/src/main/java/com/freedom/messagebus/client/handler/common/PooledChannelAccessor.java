package com.freedom.messagebus.client.handler.common;

import com.freedom.messagebus.client.IChannelDestroyer;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.core.pool.AbstractPool;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.rabbitmq.client.Channel;
import org.jetbrains.annotations.NotNull;

/**
 * pooled channel accessor used to access a channel from a pool
 * the pool based on apache common pool (v2.0)
 */
public class PooledChannelAccessor extends AbstractHandler {

    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    @Override
    public void handle(@NotNull MessageContext context,
                       @NotNull IHandlerChain chain) {

        final AbstractPool<Channel> pool = context.getPool();

        context.setChannel(pool.getResource());

        //set channel destroyer , just return here
        context.setDestroyer(new IChannelDestroyer() {
            @Override
            public void destroy(@NotNull Channel channel) {
                pool.returnResource(channel);
            }
        });

        chain.handle(context);
    }
}
