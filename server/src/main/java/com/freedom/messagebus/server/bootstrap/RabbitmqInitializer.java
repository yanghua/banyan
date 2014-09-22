package com.freedom.messagebus.server.bootstrap;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RabbitmqInitializer {

    private static Log logger = LogFactory.getLog(RabbitmqInitializer.class);
    private static volatile RabbitmqInitializer instance = null;

    public static RabbitmqInitializer getInstance() {
        if (instance == null) {
            synchronized (RabbitmqInitializer.class) {
                if (instance == null) {
                    instance = new RabbitmqInitializer();
                }
            }
        }

        return instance;
    }

    public void launch () {

    }

}
