package com.messagebus.client.event.carry;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.messagebus.client.MessageContext;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.client.model.MessageCarryType;
import com.messagebus.client.model.Node;
import com.messagebus.common.Constants;
import com.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yanghua on 6/29/15.
 */
public class BroadcastEventProcessor extends CommonEventProcessor {

    private static final Log logger = LogFactory.getLog(BroadcastEventProcessor.class);

    private static final String EVENT_ROUTING_KEY_NAME = "routingkey.proxy.message.inner.#";

    @Subscribe
    public void onValidate(ValidateEvent event) {
        logger.debug("=-=-=- event : onValidate =-=-=-");
        super.onValidate(event);
        MessageContext context = event.getMessageContext();
        if (!context.getCarryType().equals(MessageCarryType.BROADCAST)) {
            logger.error("the message carry type should be broadcast");
            throw new RuntimeException("the message carry type should be broadcast");
        }

        this.validateMessageProperties(context);
    }

    @Subscribe
    public void onPermissionCheck(PermissionCheckEvent event) {
        logger.debug("=-=-=- event : onPermissionCheck =-=-=-");
        boolean hasPermission;
        MessageContext context = event.getMessageContext();

        Node sourceNode = context.getSourceNode();
        hasPermission = sourceNode.isCanBroadcast();

        if (!hasPermission) {
            logger.error("the queue with name : " + sourceNode.getName()
                             + ", with secret : " + sourceNode.getSecret() + " can not broadcast !");
            throw new RuntimeException("the queue with name : " + sourceNode.getName()
                                           + ", with secret : " + sourceNode.getSecret() + " can not broadcast !");
        }
    }

    @Subscribe
    public void onBroadcast(BroadcastEvent event) {
        logger.debug("=-=-=- event : onBroadcast =-=-=-");
        MessageContext context = event.getMessageContext();
        try {
            for (Message msg : context.getMessages()) {

                Map<String, Object> map = new HashMap<String, Object>(1);
                map.put("type", "notice");
                msg.setHeaders(map);
                AMQP.BasicProperties properties = MessageHeaderTransfer.box(msg);

                ProxyProducer.produce(Constants.PROXY_EXCHANGE_NAME,
                                      context.getChannel(),
                                      EVENT_ROUTING_KEY_NAME,
                                      msg.getContent(),
                                      properties);

            }
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void process(MessageContext msgContext) {
        throw new UnsupportedOperationException("this method should be implemented in consume event processor!");
    }

    private void validateMessageProperties(MessageContext context) {
        Date currentDate = new Date();
        for (Message msg : context.getMessages()) {
            //app id
            if (Strings.isNullOrEmpty(msg.getAppId()))
                msg.setAppId(context.getSourceNode().getAppId());

            //timestamp
            if (msg.getTimestamp() == 0)
                msg.setTimestamp(currentDate.getTime());

            if (!MessageType.BroadcastMessage.getType().equals(msg.getType())) {
                logger.error("[validateMessagesProperites] the message's type is not Broadcast Message ");
                throw new RuntimeException(" the message's type is not  Broadcast Message ");
            }
        }
    }

    public static class ValidateEvent extends CarryEvent {
    }

    ;

    public static class PermissionCheckEvent extends CarryEvent {
    }

    public static class BroadcastEvent extends CarryEvent {
    }

}
