package com.messagebus.client.handler.rpcRequest;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.common.Constants;
import com.messagebus.common.ExceptionHelper;
import com.rabbitmq.tools.jsonrpc.JsonRpcClient;
import com.rabbitmq.tools.jsonrpc.JsonRpcException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by yanghua on 4/8/15.
 */
public class RealRpcRequest extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(RealRpcRequest.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        JsonRpcClient client = null;
        try {
            client = new JsonRpcClient(context.getChannel(),
                                                     Constants.PROXY_EXCHANGE_NAME,
                                                     context.getTargetNode().getRoutingKey(),
                                                     (int) context.getTimeout());
            Object[] params = null;
            if (context.getOtherParams().get("params") != null) {
                params = (Object[]) context.getOtherParams().get("params");
            }
            Object respObj = client.call(context.getOtherParams().get("methodName").toString(), params);
            context.getOtherParams().put("result", respObj);
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "rpc request handler : RealRpcRequest");
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            context.setIsTimeout(true);
        } catch (JsonRpcException e) {
            ExceptionHelper.logException(logger, e, "rpc request handler : RealRpcRequest");
            throw new RuntimeException(e);
        } finally {
            try {
                if (client != null) client.close();
            } catch (IOException e) {

            }
        }
    }

}
