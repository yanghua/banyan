package com.freedom.messagebus.manager;

import com.freedom.messagebus.common.AbstractInitializer;
import com.freedom.messagebus.common.model.Router;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * User: yanghua
 * Date: 7/2/14
 * Time: 10:16 AM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public final class RouterManager extends AbstractInitializer {

    private static final Log logger = LogFactory.getLog(RouterManager.class);

    public static RouterManager defaultRouterManager(String host) {
        return new RouterManager(host);
    }

    private RouterManager(String host) {
        super(host);
    }

    public void declareRouter(@NotNull Router router) throws IOException {
        super.init();
        this.channel.exchangeDeclare(router.getRouterName(), router.getRouterType().toString(), router.isDurable());
        super.close();
    }

    public void deleteRouter(@NotNull Router router) throws IOException {
        super.init();
        this.channel.exchangeDelete(router.getRouterName());
        super.close();
    }

    public boolean isRouterExists(@NotNull Router router) throws IOException {
        super.init();
        boolean result = true;
        try {
            this.channel.exchangeDeclarePassive(router.getRouterName());
        } catch (IOException e) {
            result = false;
        }
        super.close();

        return result;
    }

}
