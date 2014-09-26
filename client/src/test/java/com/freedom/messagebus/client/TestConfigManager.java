package com.freedom.messagebus.client;

import com.freedom.messagebus.client.core.config.ConfigManager;
import junit.framework.TestCase;

public class TestConfigManager extends TestCase {

    ConfigManager configManager = ConfigManager.getInstance();

    @Override
    public void setUp() throws Exception {

    }

    @Override
    public void tearDown() throws Exception {

    }

    public void testParseRouterInfo() throws Exception {
        configManager.parseRouterInfo();

    }
}
