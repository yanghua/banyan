package com.messagebus.client.event.carry;

import com.google.common.eventbus.Subscribe;
import com.messagebus.client.MessageContext;
import com.messagebus.client.model.Node;
import com.messagebus.common.Constants;
import com.messagebus.common.ExceptionHelper;
import com.rabbitmq.tools.jsonrpc.JsonRpcClient;
import com.rabbitmq.tools.jsonrpc.JsonRpcException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by yanghua on 6/29/15.
 */
public class RpcRequestEventProcessor extends CommonEventProcessor {

    private static final Log logger = LogFactory.getLog(RpcRequestEventProcessor.class);

    public RpcRequestEventProcessor() {
    }

    @Subscribe
    public void onPermissionCheck(PermissionCheckEvent event) {
        logger.debug("=-=-=- event : onPermissionCheck =-=-=-");
        MessageContext context = event.getMessageContext();
        Node sourceNode = context.getSourceNode();
        Node targetNode = context.getTargetNode();
        boolean hasPermission;

        String token = context.getToken();

        hasPermission = context.getConfigManager().getNodeView(context.getSecret()).getSinkTokens().contains(token);

        if (!hasPermission) {
            logger.error("[handle] can not produce message from queue [" + sourceNode.getName() +
                             "] to queue [" + targetNode.getName() + "]");
            throw new RuntimeException("can not produce message from queue [" + sourceNode.getName() +
                                           "] to queue [" + targetNode.getName() + "]");
        }
    }

    @Subscribe
    public void onRpcRequest(RpcRequestEvent event) {
        logger.debug("=-=-=- event : onRpcRequest =-=-=-");
        MessageContext context = event.getMessageContext();
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

    @Override
    public void process(MessageContext msgContext) {
        throw new UnsupportedOperationException("this method should be implemented in consume event processor!");
    }

    public static class PermissionCheckEvent extends CarryEvent {
    }

    public static class RpcRequestEvent extends CarryEvent {
    }

}
