package com.freedom.messagebus.manager;

import com.freedom.messagebus.common.RouterType;
import com.freedom.messagebus.common.model.TransferCenter;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * User: yanghua
 * Date: 7/3/14
 * Time: 9:19 AM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public class TestMessageBusManager extends TestCase {

    private String               appKey     = null;
    private boolean              durable    = true;
    private List<TransferCenter> tcList     = null;
    private MessageBusManager    manager    = null;
    private String               host       = "";
    private List<String>         tcNameList = null;

    public void setUp() throws Exception {
        this.appKey = java.util.UUID.randomUUID().toString();
        this.tcList = new ArrayList<TransferCenter>(4);
        this.tcNameList = new ArrayList<String>(4);
        this.manager = MessageBusManager.defaultMsgBusManager(this.host);

        for (RouterType type : RouterType.values()) {
            TransferCenter tc = new TransferCenter();
            tc.setAppKey(this.appKey);
            tc.setRouterType(type);
            tc.setRoutingKey("");
            tc.setDurable(this.durable);
            this.tcList.add(tc);
        }

        for (TransferCenter tc : this.tcList) {
            this.tcNameList.add(tc.getTCN());
        }
    }

    public void tearDown() throws Exception {

    }

    public void testRegister() throws Exception {
        this.manager.register(this.appKey, this.durable, this.tcList);
        Thread.sleep(30000);
        this.manager.deRegister(this.appKey, this.tcNameList);
    }

    public void testDeregister() throws Exception {
        this.manager.register(this.appKey, this.durable, this.tcList);
        Thread.sleep(30000);
        this.manager.deRegister(this.appKey, this.tcNameList);
    }
}
