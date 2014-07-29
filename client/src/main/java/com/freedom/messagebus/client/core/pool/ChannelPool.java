package com.freedom.messagebus.client.core.pool;

import com.rabbitmq.client.Channel;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.jetbrains.annotations.NotNull;

/**
 * the implementation of AbstractPool
 */
public class ChannelPool extends AbstractPool<Channel> {

    public ChannelPool(@NotNull GenericObjectPoolConfig poolConfig,
                       @NotNull PooledObjectFactory<Channel> factory) {
        super(poolConfig, factory);
    }

    @Override
    public Channel getResource() {
        return super.getResource();
    }

    @Override
    public void returnResource(@NotNull Channel resource) {
        super.returnResource(resource);
    }

    @Override
    public void returnBrokenResource(@NotNull Channel resource) {
        super.returnBrokenResource(resource);
    }
}
