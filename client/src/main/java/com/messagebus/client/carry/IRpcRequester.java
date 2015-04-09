package com.messagebus.client.carry;

/**
 * Created by yanghua on 4/8/15.
 */
public interface IRpcRequester {

    public Object call(String secret, String target, String methodName, Object[] params, String token, long timeoutOfMilliSecond);

}
