package com.freedom.messagebus.client.core.message;

import java.io.Serializable;

/**
 * the object message interface
 */
@Deprecated
public interface ObjectMessage extends Message {

    /**
     * get real serialized object
     *
     * @return the real serializable object
     */
    public Serializable getObject();

    /**
     * set the real serializable object
     *
     * @param object the serialized object
     */
    public void setObject(Serializable object);

}
