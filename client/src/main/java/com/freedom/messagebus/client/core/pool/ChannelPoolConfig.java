package com.freedom.messagebus.client.core.pool;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * channel pool config
 */
public class ChannelPoolConfig extends GenericObjectPoolConfig {

    public ChannelPoolConfig() {
        setTestWhileIdle(true);
        setMinEvictableIdleTimeMillis(60000);
        setTimeBetweenEvictionRunsMillis(30000);
        setNumTestsPerEvictionRun(-1);
    }
}
