package com.messagebus.client.model;

import java.io.Serializable;

/**
 * the model of handler.xml's element
 */
public class HandlerModel extends BaseModel implements Serializable {

    private String handlerName;
    private String handlerPath;

    public HandlerModel() {
    }

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public String getHandlerPath() {
        return handlerPath;
    }

    public void setHandlerPath(String handlerPath) {
        this.handlerPath = handlerPath;
    }

    @Override
    public String toString() {
        return "Handler{" +
                "handlerName='" + handlerName + '\'' +
                ", handlerPath='" + handlerPath + '\'' +
                "} ";
    }
}
