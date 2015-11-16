package com.messagebus.client.event.carry;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.messagebus.client.MessageContext;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.client.model.MessageCarryType;
import com.messagebus.client.model.Node;
import com.messagebus.common.Constants;
import com.messagebus.common.ExceptionHelper;
import com.messagebus.common.compress.CompressorFactory;
import com.messagebus.common.compress.ICompressor;
import com.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yanghua on 6/26/15.
 */
public class PublishEventProcessor extends CommonEventProcessor {

    private static final Log logger = LogFactory.getLog(PublishEventProcessor.class);

    public PublishEventProcessor() {
    }

    @Subscribe
    public void onValidate(ValidateEvent event) {
        logger.debug("=-=-=- event : onValidate =-=-=-");
        super.onValidate(event);
        MessageContext context = event.getMessageContext();
        if (!context.getCarryType().equals(MessageCarryType.PUBLISH)) {
            throw new RuntimeException("message carry type should be publish ");
        }

        this.validateMessagesProperties(context);
    }

    @Subscribe
    public void onPermissionCheck(PermissionCheckEvent event) {
        logger.debug("=-=-=- event : onPermissionCheck =-=-=-");
        MessageContext context = event.getMessageContext();
        Node sourceNode = context.getSourceNode();
        boolean hasPermission = true;

        hasPermission = sourceNode.getCommunicateType().equals("publish")
            || sourceNode.getCommunicateType().equals("publish-subscribe");

        if (!hasPermission) {
            throw new RuntimeException("can not publish message! maybe the communicate is error. "
                                           + " secret is : " + context.getSecret());
        }
    }

    @Subscribe
    public void onPublish(PublishEvent event) {
        logger.debug("=-=-=- event : onPublish =-=-=-");
        MessageContext context = event.getMessageContext();
        try {
            for (Message msg : context.getMessages()) {
                AMQP.BasicProperties properties = MessageHeaderTransfer.box(msg);

                ProxyProducer.produce(Constants.PROXY_EXCHANGE_NAME,
                                      context.getChannel(),
                                      context.getSourceNode().getRoutingKey(),
                                      msg.getContent(),
                                      properties);
            }
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "handle");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void process(MessageContext msgContext) {
        throw new UnsupportedOperationException("this method should be implemented in consume event processor!");
    }

    //region publish events definition
    public static class ValidateEvent extends CarryEvent {
    }

    public static class PermissionCheckEvent extends CarryEvent {
    }

    public static class PublishEvent extends CarryEvent {
    }
    //endregion

}
