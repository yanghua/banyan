package com.messagebus.client.carry;

import com.messagebus.business.model.Node;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.MessageCarryHandlerChain;
import com.messagebus.client.model.MessageCarryType;
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
        ctx.setSourceNode(this.getContext().getConfigManager().getSecretNodeMap().get(secret));
        ctx.setTimeout(timeoutOfMilliSecond);
        Node node = this.getContext().getConfigManager().getRpcReqRespNodeMap().get(target);
        ctx.setTargetNode(node);
        Map<String, Object> otherParams = ctx.getOtherParams();
        otherParams.put("methodName", methodName);
        otherParams.put("params", params);

        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.RPCREQUEST, this.getContext());
        this.handlerChain.handle(ctx);

        return otherParams.get("result");
    }


}
