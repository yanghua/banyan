package com.messagebus.client.event.carry;

import com.google.common.eventbus.Subscribe;
import com.messagebus.client.ConfigManager;
import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.MessageContext;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by yanghua on 6/27/15.
 */
public class SubscribeEventProcessor extends CommonEventProcessor {

    private static final Log logger = LogFactory.getLog(SubscribeEventProcessor.class);

    @Subscribe
    public void onValidate(ValidateEvent event) {
        super.exceptionCheck(event);
        logger.debug("=-=-=- event : onValidate =-=-=-");
        super.onValidate(event);
    }

    @Subscribe
    public void onPermissionCheck(PermissionCheckEvent event) {
        super.exceptionCheck(event);
        logger.debug("=-=-=- event : onPermissionCheck =-=-=-");
        MessageContext       context = event.getMessageContext();
        ConfigManager.Sink   sink    = context.getSink();
        ConfigManager.Source source  = context.getSource();
        ConfigManager.Stream stream  = context.getStream();

        boolean hasPermission = MessageCarryType.lookup(sink.getType()).equals(MessageCarryType.SUBSCRIBE);
        hasPermission = hasPermission && (stream != null && stream.getSourceSecret().equals(source.getSecret())
                && sink.getName().equals(stream.getSinkName()));

        if (!hasPermission) {
            logger.error("permission error : can not subscribe. may be communicate type is wrong . " +
                    " current secret is : " + sink.getSecret());
            event.getMessageContext().setThrowable(new RuntimeException("permission error : can not subscribe. may be communicate type is wrong . " +
                    " current secret is : " + sink.getSecret()));
        }
    }

    @Override
    public void process(MessageContext msgContext) {
        Message                 msg             = msgContext.getConsumeMsgs().get(0);
        IMessageReceiveListener receiveListener = msgContext.getReceiveListener();
        receiveListener.onMessage(msg);
    }

    public static class ValidateEvent extends CarryEvent {
    }

    public static class PermissionCheckEvent extends CarryEvent {
    }

}
