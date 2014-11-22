package com.freedom.managesystem.pojo;

import java.io.Serializable;

public class Authorization implements Serializable {

    private int    nodeId;
    private String appId;

    public Authorization() {
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Override
    public String toString() {
        return "Authorization{" +
            "nodeId=" + nodeId +
            ", appId='" + appId + '\'' +
            '}';
    }
}
