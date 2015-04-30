package com.messagebus.service.bootstrap;

import org.apache.log4j.PropertyConfigurator;

/**
 * Created by yanghua on 4/28/15.
 */
public class LogConfigInitializer {

    private static volatile LogConfigInitializer instance = null;
    private String logConfigFilePath;

    private LogConfigInitializer(String logConfigFilePath) {
        this.logConfigFilePath = logConfigFilePath;
    }

    public static LogConfigInitializer defaultConfigInitializer(String logConfigFilePath) {
        if (instance == null) {
            synchronized (LogConfigInitializer.class) {
                if (instance == null) {
                    instance = new LogConfigInitializer(logConfigFilePath);
                }
            }
        }

        return instance;
    }

    public void launch() {
        PropertyConfigurator.configure(this.logConfigFilePath);
    }
}
