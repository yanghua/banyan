package com.freedom.messagebus.httpbridge.listener;

import com.freedom.messagebus.client.Messagebus;
import com.freedom.messagebus.client.MessagebusConnectedFailedException;
import com.freedom.messagebus.httpbridge.util.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class AppContextListener implements ServletContextListener {

    private static final Log logger = LogFactory.getLog(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Messagebus messagebus = Messagebus.getInstance(Constants.HTTP_BRIDGE_APP_KEY);
        //TODO:
        messagebus.setZkHost("115.29.96.85");
        messagebus.setZkPort(2181);
        try {
            messagebus.open();
            servletContextEvent.getServletContext().setAttribute(Constants.MESSAGE_BUS_KEY, messagebus);
        } catch (MessagebusConnectedFailedException e) {
            logger.error("[contextInitialized] occurs a MessagebusConnectedFailedException : " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        Messagebus messagebus = (Messagebus) servletContextEvent.getServletContext().getAttribute(Constants.MESSAGE_BUS_KEY);
        if (messagebus != null && messagebus.isOpen()) {
            messagebus.close();
        }
    }

}
