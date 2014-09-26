package com.freedom.messagebus.client.model.message;

import com.freedom.messagebus.client.core.message.ObjectMessage;

import java.io.Serializable;

/**
 * the pojo that implement the ObjectMessage's interface
 */
@Deprecated
public class ObjectMessagePOJO implements ObjectMessage {

    /**
     * store serialized object
     */
    private Serializable originalObj;

    @Override
    public Serializable getObject() {
        return originalObj;
    }

    @Override
    public void setObject(Serializable object) {
        this.originalObj = object;
    }
}
