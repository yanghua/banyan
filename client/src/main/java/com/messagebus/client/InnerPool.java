package com.messagebus.client;

import com.messagebus.business.exchanger.ExchangerManager;
import com.messagebus.client.core.config.ConfigManager;
import com.rabbitmq.client.Connection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Created by yanghua on 3/5/15.
 */
class InnerPool extends AbstractPool<Messagebus> {

    public InnerPool(GenericObjectPoolConfig poolConfig,
                     String pubsuberHost,
                     int pubsuberPort,
                     ExchangerManager exchangeManager,
                     ConfigManager configManager,
                     Connection connection) {
        super(poolConfig, new MessagebusFactory(pubsuberHost,
                                                pubsuberPort,
                                                exchangeManager,
                                                configManager,
                                                connection)
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
