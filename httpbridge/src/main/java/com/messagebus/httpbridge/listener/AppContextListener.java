package com.messagebus.httpbridge.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class AppContextListener implements ServletContextListener {

    private static final Log logger = LogFactory.getLog(AppContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
//        MessagebusSinglePool singlePool = new MessagebusSinglePool()
//        Messagebus messagebus = new Messagebus();
//        //TODO:
//        messagebus.setPubsuberHost("115.29.96.85");
//        messagebus.setPubsuberPort(2181);
//        try {
//            messagebus.open();
//            servletContextEvent.getServletContext().setAttribute(Constants.MESSAGE_BUS_KEY, messagebus);
//        } catch (MessagebusConnectedFailedException e) {
//            logger.error("[contextInitialized] occurs a MessagebusConnectedFailedException : " + e.getMessage());
//        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
//        Messagebus messagebus = (Messagebus) servletContextEvent.getServletContext().getAttribute(Constants.MESSAGE_BUS_KEY);
//        if (messagebus != null && messagebus.isOpen()) {
//            messagebus.close();
//        }
    }

}
