package com.messagebus.client.event.carry;

import com.google.common.eventbus.Subscribe;
import com.messagebus.client.MessageContext;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.client.model.MessageCarryType;
import com.messagebus.client.model.Node;
import com.messagebus.common.Constants;
import com.messagebus.common.ExceptionHelper;
import com.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Created by yanghua on 6/25/15.
 */
public class ProduceEventProcessor extends CommonEventProcessor {

    private static final Log logger = LogFactory.getLog(ProduceEventProcessor.class);

    public ProduceEventProcessor() {
    }

    @Subscribe
    public void onValidate(ValidateEvent event) {
        logger.debug("=-=-=- event : onValidate =-=-=-");
        super.onValidate(event);

        MessageContext context = event.getMessageContext();
        Node currentNode = context.getSourceNode();
        if (!context.getCarryType().equals(MessageCarryType.PRODUCE)) {
            logger.error("message carry type should be produce, but current is : "
                             + context.getCarryType()
                             + "; node name : " + currentNode.getName()
                             + "; node secret is : " + currentNode.getSecret());
            throw new RuntimeException("message carry type should be produce, but current is : "
                                           + context.getCarryType()
                                           + "; node name : " + currentNode.getName()
                                           + "; node secret is : " + currentNode.getSecret());
        }

        if (context.getTargetNode() == null) {
            logger.error("target node can not be null "
                             + "; node name : " + currentNode.getName()
                             + "; node secret is : " + currentNode.getSecret());
            throw new RuntimeException("target node can not be null "
                                           + "; node name : " + currentNode.getName()
                                           + "; node secret is : " + currentNode.getSecret());
        }

        if (context.getTargetNode().getType().equals("0")) {
            logger.error("target node's type is illegal , "
                             + "target node name : " + context.getTargetNode().getName());
            throw new RuntimeException("target node's type is illegal , "
                                           + "target node name : " + context.getTargetNode().getName());
        }

        this.validateMessagesProperties(context);
    }

    @Subscribe
    public void onPermissionCheckEvent(PermissionCheckEvent event) {
        logger.debug("=-=-=- event : onPermissionCheckEvent =-=-=-");
        MessageContext context = event.getMessageContext();
        Node sourceNode = context.getSourceNode();
        Node targetNode = context.getTargetNode();

        boolean hasPermission = true;
        if (!hasPermission) {
            logger.error("[handle] can not produce message from queue [" + sourceNode.getName() +
                             "] to queue [" + targetNode.getName() + "]");
            throw new RuntimeException("can not produce message from queue [" + sourceNode.getName() +
                                           "] to queue [" + targetNode.getName() + "]");
        }

        String token = context.getToken();

        //send to itself queue
        if (token.equals(context.getSecret())) {
            hasPermission = sourceNode.getNodeId().equals(targetNode.getNodeId());
            hasPermission = hasPermission && sourceNode.getCommunicateType().equals(Constants.COMMUNICATE_TYPE_PRODUCE_CONSUME);
        } else {
            hasPermission = hasPermission && context.getConfigManager().getNodeView(context.getSecret()).getSinkTokens().contains(token);
        }

        if (!hasPermission) {
            logger.error("[handle] can not produce message from queue [" + sourceNode.getName() +
                             "] to queue [" + targetNode.getName() + "]");
            throw new RuntimeException("can not produce message from queue [" + sourceNode.getName() +
                                           "] to queue [" + targetNode.getName() + "]");
        }
    }

    @Subscribe
    public void onProduce(ProduceEvent event) {
        logger.debug("=-=-=- event : onProduce =-=-=-");
        MessageContext context = event.getMessageContext();
        try {
            if (context.isEnableTransaction()) {
                for (Message msg : context.getMessages()) {
                    AMQP.BasicProperties properties = MessageHeaderTransfer.box(msg);
                    ProxyProducer.produceWithTX(Constants.PROXY_EXCHANGE_NAME,
                                                context.getChannel(),
                                                context.getTargetNode().getRoutingKey(),
                                                msg.getContent(),
                                                properties);
                }
            } else {
                for (Message msg : context.getMessages()) {
                    AMQP.BasicProperties properties = MessageHeaderTransfer.box(msg);

                    ProxyProducer.produce(Constants.PROXY_EXCHANGE_NAME,
                                          context.getChannel(),
                                          context.getTargetNode().getRoutingKey(),
                                          msg.getContent(),
                                          properties);
                }
            }

        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "real produce");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void process(MessageContext msgContext) {
        throw new UnsupportedOperationException("this method should be implemented in consume event processor!");
    }

    //region events definition
    public static class ValidateEvent extends CarryEvent {
    }


    public static class PermissionCheckEvent extends CarryEvent {
    }

    public static class ProduceEvent extends CarryEvent {
    }
    //endregion

}
