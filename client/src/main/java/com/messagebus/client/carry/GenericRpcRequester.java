package com.messagebus.client.carry;

import com.google.common.eventbus.EventBus;
import com.messagebus.client.MessageContext;
import com.messagebus.client.event.carry.RpcRequestEventProcessor;
import com.messagebus.client.model.MessageCarryType;
import com.messagebus.client.model.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * Created by yanghua on 4/8/15.
 */
public class GenericRpcRequester extends AbstractMessageCarryer implements IRpcRequester {

    private static final Log logger = LogFactory.getLog(GenericRpcRequester.class);

    public GenericRpcRequester() {
    }

    @Override
    public Object call(String secret, String target, String methodName, Object[] params, String token, long timeoutOfMilliSecond) {
        MessageContext ctx = initMessageContext();
        ctx.setSecret(secret);
        ctx.setToken(token);
        ctx.setCarryType(MessageCarryType.RPCREQUEST);
        ctx.setSourceNode(this.getContext().getConfigManager().getNodeView(secret).getCurrentQueue());
        ctx.setTimeout(timeoutOfMilliSecond);
        Node node = this.getContext().getConfigManager().getNodeView(secret).getRelatedQueueNameNodeMap().get(target);
        ctx.setTargetNode(node);
        Map<String, Object> otherParams = ctx.getOtherParams();
        otherParams.put("methodName", methodName);
        otherParams.put("params", params);

        this.innerRpcRequest(ctx);

        return otherParams.get("result");
    }

    private void innerRpcRequest(MessageContext ctx) {
        checkState();

        EventBus carryEventBus = this.getContext().getCarryEventBus();

        //register event processor
        RpcRequestEventProcessor eventProcessor = new RpcRequestEventProcessor();
        carryEventBus.register(eventProcessor);

        RpcRequestEventProcessor.PermissionCheckEvent permissionCheckEvent = new RpcRequestEventProcessor.PermissionCheckEvent();
        RpcRequestEventProcessor.RpcRequestEvent rpcRequestEvent = new RpcRequestEventProcessor.RpcRequestEvent();

        permissionCheckEvent.setMessageContext(ctx);
        rpcRequestEvent.setMessageContext(ctx);

        carryEventBus.post(permissionCheckEvent);
        carryEventBus.post(rpcRequestEvent);

        //unregister
        carryEventBus.unregister(eventProcessor);
    }

}
