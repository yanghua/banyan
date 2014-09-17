package com.freedom.messagebus.common.message.messageBody;

import com.freedom.messagebus.common.message.IMessageBody;

import java.io.Serializable;

public class LookuprespMessageBody implements IMessageBody, Serializable {

    private String exchangeName;
    private String routingKey;

    public LookuprespMessageBody() {
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }
}
