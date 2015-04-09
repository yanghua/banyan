package com.messagebus.client.core;

import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.common.TestVariableInfo;
import junit.framework.TestCase;

/**
 * Created by yanghua on 3/20/15.
 */
public class BaseTestCase extends TestCase {

    protected String host = TestVariableInfo.PUBSUBER_HOST;
    protected int    port = TestVariableInfo.PUBSUBER_PORT;

    protected MessagebusSinglePool singlePool;
    protected Messagebus           client;

    @Override
    public void setUp() throws Exception {
        singlePool = new MessagebusSinglePool(host, port);
        client = singlePool.getResource();
    }

    @Override
    public void tearDown() throws Exception {
        singlePool.returnResource(client);
        singlePool.destroy();
    }

    public void testVirtual() throws Exception {
    }
}
