package org.ofbiz.banyan.listener;

import com.google.common.base.Strings;
import com.messagebus.client.MessagebusPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.ofbiz.banyan.common.Constants;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.cache.UtilCache;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by yanghua on 3/22/15.
 */
public class MBClientPoolListener implements ServletContextListener {

    public static String module = MBClientPoolListener.class.getName();
    private MessagebusPool messagebusPool;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Debug.logInfo("initing messagebus pool ...", module);
        String pubsuberHost = UtilProperties.getPropertyValue("MessagebusConfig", "messagebus.pubsuberHost");

        if (Strings.isNullOrEmpty(pubsuberHost)) {
            Debug.logError("missing config item : messagebus.pubsuberHost in config/MessagebusConfig.properties", module);
        }

        String pubsuberPortStr = UtilProperties.getPropertyValue("MessagebusConfig", "messagebus.pubsuberPort");
        if (Strings.isNullOrEmpty(pubsuberPortStr)) {
            Debug.logError("missing config item : messagebus-pubsubPort in config/MessagebusConfig.properties", module);
        }

        int pubsuberPort = Integer.parseInt(pubsuberPortStr);

        String maxTotalStr = UtilProperties.getPropertyValue("MessagebusConfig", "messagebus.pool.maxTotal");
        if (Strings.isNullOrEmpty(maxTotalStr)) {
            Debug.logError("missing config item : messagebus-pool-maxTotal in config/MessagebusConfig.properties", module);
        }

        int maxTotal = Integer.parseInt(maxTotalStr);

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(maxTotal);
        messagebusPool = new MessagebusPool(pubsuberHost, pubsuberPort, poolConfig);

        //set instance to cache
        UtilCache<String, Object> poolCache = UtilCache.findCache(Constants.KEY_OF_BANYAN_GLOBAL_CACHE);
        poolCache.put(Constants.KEY_OF_MESSAGEBUS_POOL, messagebusPool);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        Debug.logInfo("destroying messagebus pool...", module);
        if (messagebusPool != null) {
            messagebusPool.destroy();
        }
    }

}
