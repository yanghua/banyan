package com.freedom.messagebus.server.bootstrap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ZookeeperInitializer {

    private static Log logger = LogFactory.getLog(ZookeeperInitializer.class);
    private static volatile ZookeeperInitializer instance = null;

    public static ZookeeperInitializer getInstance() {
        if (instance == null) {
            synchronized (ZookeeperInitializer.class) {
                if (instance == null) {
                    instance = new ZookeeperInitializer();
                }
            }
        }

        return instance;
    }

    public void launch() {
        //TODO
    }

}
