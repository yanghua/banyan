package com.messagebus.client;

/**
 * Created by yanghua on 4/17/15.
 */
public interface IRpcMessageProcessor {

    public byte[] onRpcMessage(byte[] in);

}
