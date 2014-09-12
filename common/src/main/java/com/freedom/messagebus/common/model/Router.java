package com.freedom.messagebus.common.model;

import com.freedom.messagebus.common.CommonUtil;
import com.freedom.messagebus.common.RouterType;
import org.jetbrains.annotations.NotNull;

/**
 * User: yanghua
 * Date: 7/2/14
 * Time: 5:00 PM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public class Router extends BaseModel {

    private String     appKey;
    private RouterType routerType;
    private boolean    durable;

    public Router() {
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public RouterType getRouterType() {
        return routerType;
    }

    public void setRouterType(RouterType routerType) {
        this.routerType = routerType;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    @NotNull
    public String getRouterName() {
        return CommonUtil.getRouterName(this.appKey, this.routerType);
    }

    @Override
    public String toString() {
        return "Router{" +
            "appKey='" + appKey + '\'' +
            ", routerType=" + routerType +
            ", durable=" + durable +
            "} " + super.toString();
    }
}
