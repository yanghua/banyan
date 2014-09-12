package com.freedom.managesystem.service;

import com.freedom.managesystem.pojo.Config;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration(locations = "classpath:applicationContextForTest.xml")
public class ConfigServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private IConfigService configService;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGet() throws Exception {
        long id = 1l;
        Config config = configService.get(id);
        Assert.assertNotNull(config);
        Assert.assertEquals("test", config.getKey());

    }
}
