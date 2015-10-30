package com.messagebus.client;

import com.google.common.eventbus.EventBus;
import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by yanghua on 3/5/15.
 */
class InnerPool extends AbstractPool<Messagebus> {

    public InnerPool(GenericObjectPoolConfig poolConfig,
                     String pubsuberHost,
                     int pubsuberPort,
                     ConfigManager configManager,
                     Connection connection,
                     EventBus componentEventBus) {
        super(poolConfig, new MessagebusFactory(pubsuberHost,
                                                pubsuberPort,
                                                configManager,
                                                connection,
                                                componentEventBus)
             );
    }

    /**
     * inner class : default messagebus pool
     */
    private static class DefaultMessagebusPool extends GenericObjectPoolConfig {

        public DefaultMessagebusPool() {
            setTestWhileIdle(false);
            setMinEvictableIdleTimeMillis(60000);
            setTimeBetweenEvictionRunsMillis(30000);
            setNumTestsPerEvictionRun(-1);
        }

    }

}
