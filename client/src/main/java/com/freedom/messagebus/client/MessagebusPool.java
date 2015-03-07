package com.freedom.messagebus.client;

import com.freedom.messagebus.client.core.pool.AbstractPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by yanghua on 3/5/15.
 */
public class MessagebusPool extends AbstractPool<Messagebus> {

    public MessagebusPool(GenericObjectPoolConfig poolConfig, String appId, String pubsuberHost, int pubsuberPort) {
        super(poolConfig, new MessagebusFactory(appId, pubsuberHost, pubsuberPort));
    }

    public MessagebusPool(String appId, String pubsuberHost, int pubsuberPort) {
        super(new DefaultMessagebusPool(), new MessagebusFactory(appId, pubsuberHost, pubsuberPort));
    }

    private static class DefaultMessagebusPool extends GenericObjectPoolConfig {

        public DefaultMessagebusPool() {
            setTestWhileIdle(false);
            setMinEvictableIdleTimeMillis(60000);
            setTimeBetweenEvictionRunsMillis(30000);
            setNumTestsPerEvictionRun(-1);
        }
    }

}
