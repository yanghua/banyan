package com.messagebus.common.configuration;

/**
 * Created by yanghua on 2/6/15.
 */
public interface IPubSubListener {

    public void onChange(String channel, ZKEventType eventType);

}
