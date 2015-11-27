package com.messagebus.client.carry;

import com.messagebus.client.IRpcMessageProcessor;
import com.messagebus.client.WrappedRpcServer;

import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 4/8/15.
 */
public interface IRpcResponser {

    public void callback(String secret, Class<?> clazzOfInterface, Object serviceProvider, long timeout, TimeUnit timeUnit);

    public WrappedRpcServer buildRpcServer(String secret, IRpcMessageProcessor rpcMsgProcessor);
}
