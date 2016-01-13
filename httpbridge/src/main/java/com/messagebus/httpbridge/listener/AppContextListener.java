package com.messagebus.httpbridge.listener;

import com.messagebus.client.MessagebusPool;
import com.messagebus.httpbridge.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class AppContextListener implements ServletContextListener {

    private static final Log logger = LogFactory.getLog(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        String pubsuberHost = servletContext.getInitParameter("pubsuberHost");
        int pubsuberPort = Integer.parseInt(servletContext.getInitParameter("pubsuberPort"));

        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(Integer.parseInt(servletContext.getInitParameter("messagebuspool.maxtotal")));
        MessagebusPool messagebusPool = new MessagebusPool(pubsuberHost, poolConfig);
        servletContextEvent.getServletContext().setAttribute(Constants.KEY_OF_MESSAGEBUS_POOL_OBJ, messagebusPool);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        MessagebusPool messagebusPool = (MessagebusPool) servletContextEvent.getServletContext().getAttribute(Constants.KEY_OF_MESSAGEBUS_POOL_OBJ);
        if (messagebusPool != null) {
            messagebusPool.destroy();
        }
    }

}
