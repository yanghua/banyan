package com.messagebus.interactor.rabbitmq;

import java.io.Serializable;

/**
 * Created by yanghua on 11/20/15.
 */
public class Queue implements Serializable {

    private String queueName;
    private String bindExchange;
    private String routingKey;
    private String typeId;
    private int    threshold;
    private int    msgBodySize;
    private int    ttl;
    private int    ttlPerMsg;

    public Queue() {
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getBindExchange() {
        return bindExchange;
    }

    public void setBindExchange(String bindExchange) {
        this.bindExchange = bindExchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getMsgBodySize() {
        return msgBodySize;
    }

    public void setMsgBodySize(int msgBodySize) {
        this.msgBodySize = msgBodySize;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public int getTtlPerMsg() {
        return ttlPerMsg;
    }

    public void setTtlPerMsg(int ttlPerMsg) {
        this.ttlPerMsg = ttlPerMsg;
    }
}
