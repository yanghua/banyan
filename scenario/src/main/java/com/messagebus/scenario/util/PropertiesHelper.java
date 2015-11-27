package com.messagebus.scenario.util;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by yanghua on 3/25/15.
 */
public class PropertiesHelper {

    private static final Properties commonProperties;

    static {
        commonProperties = new Properties();
        try {
            commonProperties.load(PropertiesHelper.class.getClassLoader().getResourceAsStream("common.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPropertyValue(String key) {
        return commonProperties.getProperty(key);
    }

}
