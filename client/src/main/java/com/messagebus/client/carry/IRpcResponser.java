package com.messagebus.client.carry;

import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 4/8/15.
 */
public interface IRpcResponser {

    public void callback(String secret, Class<?> clazzOfInterface, Object serviceProvider, long timeout, TimeUnit timeUnit);

}
