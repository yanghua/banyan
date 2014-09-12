package com.freedom.messagebus.manager;

import com.freedom.messagebus.common.RouterType;
import com.freedom.messagebus.common.model.Router;
import com.freedom.messagebus.common.model.TransferCenter;
import junit.framework.TestCase;

/**
 * User: yanghua
 * Date: 7/3/14
 * Time: 9:20 AM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public class TestTransferCenterManager extends TestCase {

    private String         appKey = "";
    private Router         router = null;
    private TransferCenter tc     = null;
    private String         host   = "";

    private TransferCenterManager manager       = null;
    private RouterManager         routerManager = null;

    public void setUp() throws Exception {
        this.appKey = java.util.UUID.randomUUID().toString();
        this.manager = TransferCenterManager.defaultTransferCenterManager(this.host);
        this.routerManager = RouterManager.defaultRouterManager(this.host);

        router = new Router();
        router.setAppKey(this.appKey);
        router.setRouterType(RouterType.DIRECT);
        router.setDurable(true);

        tc = new TransferCenter();
        tc.setDurable(true);
        tc.setAppKey(this.appKey);
        tc.setRouterType(RouterType.DIRECT);
        tc.setRoutingKey("");

        this.routerManager.declareRouter(router);
    }

    public void tearDown() throws Exception {
        this.routerManager.deleteRouter(router);
    }

    public void testCreateTransferCenter() throws Exception {
        this.manager.createTransferCenter(router, tc);
        Thread.sleep(15000);
        this.manager.deleteTransferCenter(tc);
    }

    public void testDeleteTransfer() throws Exception {
        this.manager.createTransferCenter(router, tc);
        Thread.sleep(15000);
        this.manager.deleteTransferCenter(tc);
    }

    public void testDeclareTransferCenter() throws Exception {
        this.manager.declareTransferCenter(tc);
        Thread.sleep(15000);
        this.manager.deleteTransferCenter(tc);
    }

    public void testBindTransferCenter() throws Exception {
        this.manager.declareTransferCenter(tc);
        this.manager.bindTransferCenter(router, tc);
        Thread.sleep(15000);
        this.manager.deleteTransferCenter(tc);
    }

    public void testIsTransferCenterExists() throws Exception {
        boolean exists = this.manager.isTransferCenterExists(tc);
        assertEquals(false, exists);
        this.manager.declareTransferCenter(tc);
        exists = this.manager.isTransferCenterExists(tc);
        assertEquals(true, exists);
        this.manager.deleteTransferCenter(tc);
    }
}
