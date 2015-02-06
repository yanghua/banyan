package com.freedom.messagebus.client.core.pool;

import com.rabbitmq.client.Channel;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * the implementation of AbstractPool
 */
public class ChannelPool extends AbstractPool<Channel> {

    public ChannelPool(GenericObjectPoolConfig poolConfig,
                       PooledObjectFactory<Channel> factory) {
        super(poolConfig, factory);
    }

    @Override
    public Channel getResource() {
        return super.getResource();
    }

    @Override
    public void returnResource(Channel resource) {
        super.returnResource(resource);
    }

    @Override
    public void returnBrokenResource(Channel resource) {
        super.returnBrokenResource(resource);
    }
}
