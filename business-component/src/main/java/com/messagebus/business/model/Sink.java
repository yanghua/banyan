package com.messagebus.business.model;

import java.io.Serializable;

/**
 * Created by yanghua on 3/16/15.
 */
public class Sink implements Serializable {

    private String token;
    private String flowFrom;
    private String flowTo;

    public Sink() {
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getFlowFrom() {
        return flowFrom;
    }

    public void setFlowFrom(String flowFrom) {
        this.flowFrom = flowFrom;
    }

    public String getFlowTo() {
        return flowTo;
    }

    public void setFlowTo(String flowTo) {
        this.flowTo = flowTo;
    }
}
