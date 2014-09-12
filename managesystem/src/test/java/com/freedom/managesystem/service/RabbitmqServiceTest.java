package com.freedom.managesystem.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(locations = "classpath:applicationContextForTest.xml")
public class RabbitmqServiceTest extends AbstractJUnit4SpringContextTests {

    private static final Log logger = LogFactory.getLog(RabbitmqServiceTest.class);

    @Autowired
    IRabbitmqService rabbitmqService;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testOverview() throws Exception {
        String responseData = rabbitmqService.overview();
        Assert.assertNotEquals("", responseData);
        logger.info("[testOverview] : response data is : " + responseData);
    }
}
