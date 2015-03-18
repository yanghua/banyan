package com.messagebus.client;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by yanghua on 3/18/15.
 */
public class MessagebusSinglePool extends MessagebusPool {

    public MessagebusSinglePool(String pubsuberHost, int pubsuberPort) {
        super(pubsuberHost,
              pubsuberPort,
              new DefaultMessagebusPoolConfig());
    }

    /**
     * inner class : default messagebus pool
     */
    private static class DefaultMessagebusPoolConfig extends GenericObjectPoolConfig {

        public DefaultMessagebusPoolConfig() {
            setTestWhileIdle(false);
            setMaxTotal(1);
            setMinEvictableIdleTimeMillis(60000);
            setTimeBetweenEvictionRunsMillis(30000);
            setNumTestsPerEvictionRun(-1);
        }

    }
}
