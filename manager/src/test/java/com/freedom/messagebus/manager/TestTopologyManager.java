package com.freedom.messagebus.manager;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import junit.framework.TestCase;

/**
 * User: yanghua
 * Date: 7/20/14
 * Time: 3:58 PM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public class TestTopologyManager extends TestCase {

    private TopologyManager topologyManager = null;
    protected ConnectionFactory connectionFactory;
    protected Connection        connection;
    protected Channel           channel;

    public void setUp() throws Exception {
        this.connectionFactory = new ConnectionFactory();
        this.connectionFactory.setHost("115.29.96.85");

        this.connection = this.connectionFactory.newConnection();
        this.channel = this.connection.createChannel();

        topologyManager = TopologyManager.getInstance(channel);
    }

    public void tearDown() throws Exception {

    }

    public void testInit() throws Exception {
        this.topologyManager.init();
    }

    public void testDestroy() throws Exception {
        this.topologyManager.destroy();

    }
}
