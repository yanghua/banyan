package com.messagebus.client;

import com.messagebus.common.ExceptionHelper;
import com.rabbitmq.client.RpcServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Created by yanghua on 4/16/15.
 */
public class WrappedRpcServer {

    private static final Log logger = LogFactory.getLog(WrappedRpcServer.class);

    private RpcServer innerRpcServer;

    private WrappedRpcServer(RpcServer rpcServer) {
        this.innerRpcServer = rpcServer;
    }

    public void mainLoop() {
        try {
            innerRpcServer.mainloop();
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "mainLoop");
            throw new RuntimeException(e);
        }
    }

    public void terminateMainloop() {
        innerRpcServer.terminateMainloop();
    }

    public void close() {
        try {
            innerRpcServer.close();
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "close");
            throw new RuntimeException(e);
        }
    }
}
