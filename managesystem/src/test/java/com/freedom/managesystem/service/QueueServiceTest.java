package com.freedom.managesystem.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(locations = "classpath:applicationContextForTest.xml")
public class QueueServiceTest extends AbstractJUnit4SpringContextTests {

    private static final Log logger = LogFactory.getLog(QueueServiceTest.class);

    @Autowired
    private IQueueService queueService;

    public void setUp() throws Exception {

    }

    public void tearDown() throws Exception {

    }

    @Test
    public void testListAll() throws Exception {
        String responseData = queueService.listAll();
        org.springframework.util.Assert.state(!("".equals(responseData)));
        logger.info("[testListAll] response data is : " + responseData);
    }
}
