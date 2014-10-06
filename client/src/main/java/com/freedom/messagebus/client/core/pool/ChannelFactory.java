package com.freedom.messagebus.client.core.pool;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * the channel factory which implements PooledObjectFactory
 */
public class ChannelFactory implements PooledObjectFactory<Channel> {

    private Connection connection;

    public ChannelFactory(Connection connection) {
        this.connection = connection;
    }

    @Override
    public PooledObject<Channel> makeObject() throws Exception {
        Channel channel = this.connection.createChannel();
        return new DefaultPooledObject<Channel>(channel);
    }

    @Override
    public void destroyObject(PooledObject<Channel> channelPooledObject) throws Exception {
        Channel channel = channelPooledObject.getObject();

        if (channel.isOpen()) {
            try {
                channel.close();
            } finally {
                channel.abort();
            }
        }
    }

    @Override
    public boolean validateObject(PooledObject<Channel> channelPooledObject) {
        Channel channel = channelPooledObject.getObject();
        return channel.isOpen();
    }

    @Override
    public void activateObject(PooledObject<Channel> channelPooledObject) throws Exception {

    }

    @Override
    public void passivateObject(PooledObject<Channel> channelPooledObject) throws Exception {

    }
}
