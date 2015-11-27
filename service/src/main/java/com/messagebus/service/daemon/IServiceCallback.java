package com.messagebus.service.daemon;

import java.util.Map;

/**
 * Created by yanghua on 4/24/15.
 */
public interface IServiceCallback {

    public void callback(Map<String, Object> context);

}
