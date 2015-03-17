package com.messagebus.interactor.pubsub;

import java.util.Map;

/**
 * Created by yanghua on 2/6/15.
 */
public interface IPubSubListener {

    public void onChange(String channel, byte[] data, Map<String, Object> params);

}
