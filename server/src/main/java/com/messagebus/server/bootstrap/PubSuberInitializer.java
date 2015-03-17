package com.messagebus.server.bootstrap;

import com.messagebus.business.exchanger.ExchangerManager;
import com.messagebus.server.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class PubSuberInitializer {

    private static          Log                 logger   = LogFactory.getLog(PubSuberInitializer.class);
    private static volatile PubSuberInitializer instance = null;

    private Properties          config;
    private Map<String, Object> context;
    private ExchangerManager    exchangeManager;

    public static PubSuberInitializer getInstance(Map<String, Object> context) {
        if (instance == null) {
            synchronized (PubSuberInitializer.class) {
                if (instance == null) {
                    instance = new PubSuberInitializer(context);
                }
            }
        }

        return instance;
    }

    private PubSuberInitializer(Map<String, Object> context) {
        this.context = context;
        this.config = (Properties) this.context.get(Constants.KEY_SERVER_CONFIG);
        this.exchangeManager = (ExchangerManager) this.context.get(Constants.GLOBAL_EXCHANGE_MANAGER);
    }

    public void launch() throws IOException, InterruptedException {
        this.exchangeManager.uploadAll();
    }


}
