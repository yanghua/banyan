package com.freedom.messagebus.common.model;

import java.io.Serializable;

/**
 * representation a node of the topology
 */
public class Node implements Serializable,Comparable<Node> {

    private int    generatedId;
    private String name;
    private String value;
    private int    parentId;
    private short  type;         //0: exchange 1: queue
    private String routerType;
    private String routingKey;
    private short  level;

    public Node() {
    }

    public int getGeneratedId() {
        return generatedId;
    }

    public void setGeneratedId(int generatedId) {
        this.generatedId = generatedId;
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

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
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

    public short getLevel() {
        return level;
    }

    public void setLevel(short level) {
        this.level = level;
    }

    @Override
    public int compareTo(Node o) {
        if (this.generatedId == o.getGeneratedId())
            return 0;
        else if (this.generatedId < o.getGeneratedId())
            return -1;
        else
            return 1;
    }

    @Override
    public String toString() {
        return "Node{" +
            "generatedId=" + generatedId +
            ", name='" + name + '\'' +
            ", value='" + value + '\'' +
            ", parentId=" + parentId +
            ", type=" + type +
            ", routerType='" + routerType + '\'' +
            ", routingKey='" + routingKey + '\'' +
            ", level=" + level +
            '}';
    }
}
