package com.freedom.messagebus.client;

import com.rabbitmq.client.Channel;
import org.jetbrains.annotations.NotNull;

/**
 * the interface of channel destroyer used to destroy the channel
 */
public interface IChannelDestroyer {

    /**
     * destroy the channel
     * Note: if uses pool mode, the "destroy" may just mean return back the channel to the pool
     *
     * @param channel the channel between the client and the rabbitmq-server
     */
    public void destroy(@NotNull Channel channel);

}
