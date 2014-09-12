package com.freedom.messagebus.common.model;

import com.freedom.messagebus.common.CommonUtil;
import com.freedom.messagebus.common.RouterType;

/**
 * User: yanghua
 * Date: 7/2/14
 * Time: 2:57 PM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public class TransferCenter extends BaseModel {

    private String     appKey;
    private boolean    durable;
    private String     routingKey;
    private RouterType routerType;

    public TransferCenter() {
    }

    public String getAppKey() {
        return this.appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public RouterType getRouterType() {
        return routerType;
    }

    public void setRouterType(RouterType routerType) {
        this.routerType = routerType;
    }

    public String getTCN() {
        return CommonUtil.getRouterName(appKey, routerType);
    }

    @Override
    public String toString() {
        return "TransferCenter{" +
            "appKey='" + appKey + '\'' +
            ", durable=" + durable +
            ", routingKey='" + routingKey + '\'' +
            ", routerType=" + routerType +
            "} " + super.toString();
    }
}
