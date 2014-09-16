package com.freedom.messagebus.common.message.messageBody;

import com.freedom.messagebus.common.message.IMessageBody;

import java.io.Serializable;

public class LookupreqMessageBody implements IMessageBody,Serializable {

    private String type;
    private String destination;

    public LookupreqMessageBody() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
