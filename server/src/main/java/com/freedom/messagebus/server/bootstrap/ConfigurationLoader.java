package com.freedom.messagebus.server.bootstrap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ConfigurationLoader {

    private static final Log logger = LogFactory.getLog(ConfigurationLoader.class);

    private static volatile ConfigurationLoader instance = null;

    private static final String DEFAULT_CONFIG_FILE_PATH = "/etc/message.server.config.properties";

    private String configFilePathStr;
    private Properties configProperties;

    public static ConfigurationLoader defaultLoader() {
        if (instance == null) {
            synchronized (ConfigurationLoader.class) {
                if (instance == null)
                    instance = new ConfigurationLoader();
            }
        }

        return instance;
    }

    public void launch() throws FileNotFoundException,IOException {
        String filePathStr;
        if (this.configFilePathStr != null)
            filePathStr = this.configFilePathStr;
        else
            filePathStr = DEFAULT_CONFIG_FILE_PATH;

        this.configProperties = new Properties();
        this.configProperties.load(new FileInputStream(filePathStr));
    }

    public String getConfigFilePathStr() {
        return configFilePathStr;
    }

    public void setConfigFilePathStr(String configFilePathStr) {
        this.configFilePathStr = configFilePathStr;
    }

    public Properties getConfigProperties() {
        return configProperties;
    }
}
