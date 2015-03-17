package com.messagebus.client.carry;


import com.messagebus.client.IRequestListener;

import java.util.concurrent.TimeUnit;

public interface IResponser {

    /**
     * response a temp message to a named queue
     *
     * @param toTmpQueue      the temp queue name
     * @param msg             the entity of message
     * @param receiveListener
     * @param secret
     * @param requestListener
     * @param timeout
     * @param timeUnit
     */
    public void response(String secret, IRequestListener requestListener, long timeout, TimeUnit timeUnit);

}
