package com.messagebus.client.handler.rpcResponse;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.common.ExceptionHelper;
import com.rabbitmq.tools.jsonrpc.JsonRpcServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Created by yanghua on 4/8/15.
 */
public class RealRpcResponser extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(RealRpcResponser.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        Class<?> clazzOfInterface = (Class<?>) context.getOtherParams().get("clazzOfInterface");
        Object obj = context.getOtherParams().get("serviceProvider");
        JsonRpcServer server = null;
        try {
            server = new JsonRpcServer(context.getChannel(),
                                       context.getSourceNode().getValue(),
                                       clazzOfInterface,
                                       obj);
            server.mainloop();
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "real rpc responser");
            throw new RuntimeException(e);
        } finally {
            if (server != null) {
                server.terminateMainloop();
            }
        }
    }

}
