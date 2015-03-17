package com.messagebus.business.model;

import java.io.Serializable;

/**
 * representation a node of the topology
 */
public class Node implements Serializable, Comparable<Node> {

    private String  nodeId;
    private String  secret;
    private String  name;
    private String  value;
    private String  parentId;
    private String  type;         //0: exchange 1: queue
    private String  routerType;
    private String  routingKey;
    private boolean available;
    private String  appId;
    private boolean isInner;
    private String  communicateType;

    public Node() {
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRouterType() {
        return routerType;
    }

    public void setRouterType(String routerType) {
        this.routerType = routerType;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public boolean isInner() {
        return isInner;
    }

    public void setInner(boolean isInner) {
        this.isInner = isInner;
    }

    public String getCommunicateType() {
        return communicateType;
    }

    public void setCommunicateType(String communicateType) {
        this.communicateType = communicateType;
    }

    @Override
    public int compareTo(Node o) {
        if (o == null) return -1;

        if (this.nodeId.equals(o.getNodeId())) {
            return 0;
        } else if (Integer.parseInt(this.nodeId) < Integer.parseInt(o.getNodeId())) {
            return -1;
        } else {
            return 1;
        }
    }

}
