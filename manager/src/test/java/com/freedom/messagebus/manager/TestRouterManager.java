package com.freedom.messagebus.manager;

import com.freedom.messagebus.common.RouterType;
import com.freedom.messagebus.common.model.Router;
import junit.framework.TestCase;

/**
 * User: yanghua
 * Date: 7/3/14
 * Time: 9:19 AM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public class TestRouterManager extends TestCase {

    private RouterManager routerManager = null;
    private String        host          = "";
    private Router        router        = null;

    public void setUp() throws Exception {
        this.routerManager = RouterManager.defaultRouterManager(this.host);

        //build router
        router = new Router();
        router.setAppKey(java.util.UUID.randomUUID().toString());
        router.setDurable(true);
        router.setRouterType(RouterType.HEADERS);
    }

    public void tearDown() throws Exception {

    }

    public void testDeclareRouter() throws Exception {
        this.routerManager.declareRouter(router);
        //sleep 15s for checking the maintain-dashboard
        Thread.sleep(15000);
        this.routerManager.deleteRouter(router);
    }

    public void testDeleteRouter() throws Exception {
        this.routerManager.declareRouter(router);
        //sleep 15s for checking the maintain-dashboard
        Thread.sleep(15000);
        this.routerManager.deleteRouter(router);
    }

    public void testRouterExists() throws Exception {
        boolean exists = this.routerManager.isRouterExists(router);
        assertEquals(false, exists);

        this.routerManager.declareRouter(router);
        exists = this.routerManager.isRouterExists(router);
        assertEquals(true, exists);
        this.routerManager.deleteRouter(router);
    }
}
