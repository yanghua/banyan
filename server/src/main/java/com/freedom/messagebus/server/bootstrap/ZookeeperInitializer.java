package com.freedom.messagebus.server.bootstrap;

import com.freedom.messagebus.business.exchanger.ExchangerManager;
import com.freedom.messagebus.server.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class ZookeeperInitializer {

    private static          Log                  logger   = LogFactory.getLog(ZookeeperInitializer.class);
    private static volatile ZookeeperInitializer instance = null;

    private Properties          config;
    private Map<String, Object> context;
    private ExchangerManager    zkExchangeManager;

    public static ZookeeperInitializer getInstance(Map<String, Object> context) {
        if (instance == null) {
            synchronized (ZookeeperInitializer.class) {
                if (instance == null) {
                    instance = new ZookeeperInitializer(context);
                }
            }
        }

        return instance;
    }

    private ZookeeperInitializer(Map<String, Object> context) {
        this.context = context;
        this.config = (Properties) this.context.get(Constants.KEY_SERVER_CONFIG);
        this.zkExchangeManager = (ExchangerManager) this.context.get(Constants.GLOBAL_ZKEXCHANGE_MANAGER);
    }

    public void launch() throws IOException, InterruptedException {
        this.zkExchangeManager.uploadAll();
    }


}
