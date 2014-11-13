package com.freedom.managesystem.service;

import com.freedom.messagebus.common.model.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration(locations = "classpath:applicationContextForTest.xml")
public class NodeServiceTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired
    private INodeService nodeService;

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testCreate() throws Exception {
        Node node = new Node();
        node.setName("proxy");
        node.setValue("fanout");
        node.setParentId(-1);
        node.setLevel((short) 0);
        node.setType((short) 0);
        nodeService.save(node);
    }
}
