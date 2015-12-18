package com.messagebus.client.event.carry;

import com.google.common.eventbus.Subscribe;
import com.messagebus.client.ConfigManager;
import com.messagebus.client.MessageContext;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.client.model.MessageCarryType;
import com.messagebus.common.Constants;
import com.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Created by yanghua on 6/26/15.
 */
public class PublishEventProcessor extends CommonEventProcessor {

    private static final Log logger = LogFactory.getLog(PublishEventProcessor.class);

    public PublishEventProcessor() {
    }

    @Subscribe
    public void onValidate(ValidateEvent event) {
        super.exceptionCheck(event);
        logger.debug("=-=-=- event : onValidate =-=-=-");
        super.onValidate(event);
        MessageContext context = event.getMessageContext();
        if (!context.getCarryType().equals(MessageCarryType.PUBLISH)) {
            logger.error("message carry type should be publish ");
            event.getMessageContext().setThrowable(new RuntimeException("message carry type should be publish "));
            return;
        }

        this.validateMessagesProperties(context);
    }

    @Subscribe
    public void onPermissionCheck(PermissionCheckEvent event) {
        super.exceptionCheck(event);
        logger.debug("=-=-=- event : onPermissionCheck =-=-=-");
        MessageContext       context       = event.getMessageContext();
        ConfigManager.Source source        = context.getSource();
        boolean              hasPermission = false;

        hasPermission = MessageCarryType.lookup(source.getType()).equals(MessageCarryType.PUBLISH);
        if (!hasPermission) {
            logger.error("can not publish message! maybe the communicate is error. "
                    + " secret is : " + context.getSecret());
            event.getMessageContext().setThrowable(new RuntimeException("can not publish message! maybe the communicate is error. "
                    + " secret is : " + context.getSecret()));
            return;
        }
    }

    @Subscribe
    public void onPublish(PublishEvent event) {
        super.exceptionCheck(event);
        logger.debug("=-=-=- event : onPublish =-=-=-");
        MessageContext context = event.getMessageContext();
        try {
            for (Message msg : context.getMessages()) {
                AMQP.BasicProperties properties = MessageHeaderTransfer.box(msg);

                ProxyProducer.produce(Constants.PROXY_EXCHANGE_NAME,
                        context.getChannel(),
                        context.getSource().getRoutingKey(),
                        msg.getContent(),
                        properties);
            }
        } catch (IOException e) {
            logger.error(e);
            event.getMessageContext().setThrowable(e);
        } catch (Exception e) {
            logger.error(e);
            event.getMessageContext().setThrowable(e);
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
