package com.messagebus.client.event.carry;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import com.messagebus.client.ConfigManager;
import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.MessageContext;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.model.MessageCarryType;
import com.messagebus.common.ExceptionHelper;
import com.messagebus.interactor.proxy.ProxyConsumer;
import com.rabbitmq.client.GetResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yanghua on 6/26/15.
 */
public class ConsumeEventProcessor extends CommonEventProcessor {

    private static final Log logger = LogFactory.getLog(ConsumeEventProcessor.class);

    @Subscribe
    public void onValidate(ValidateEvent event) {
        logger.debug("=-=-=- event : onValidate =-=-=-");
        super.onValidate(event);
    }

    @Subscribe
    public void onPermissionCheck(PermissionCheckEvent event) {
        logger.debug("=-=-=- event : onPermissionCheck =-=-=-");
        MessageContext context = event.getMessageContext();

        ConfigManager.Sink sink = context.getSink();
        boolean hasPermission = false;
        hasPermission = MessageCarryType.lookup(sink.getType()).equals(MessageCarryType.CONSUME)
            && !Strings.isNullOrEmpty(sink.getQueueName())
            && !Strings.isNullOrEmpty(sink.getName());

        if (!hasPermission) {
            logger.error("permission error : can not consume. may be communicate type is wrong. " +
                             " current secret is : " + sink.getSecret());
            throw new RuntimeException("permission error : can not consume. may be communicate type is wrong. " +
                                           " current secret is : " + sink.getSecret());
        }
    }

    @Subscribe
    public void onSyncConsume(SyncConsumeEvent event) {
        logger.debug("=-=-=- event : onSyncConsume =-=-=-");
        MessageContext context = event.getMessageContext();
        if (context.isSync()) {
            List<Message> consumeMsgs = new ArrayList<Message>(context.getConsumeMsgNum());
            context.setConsumeMsgs(consumeMsgs);
            try {
                int countDown = context.getConsumeMsgNum();
                while (countDown-- > 0) {
                    GetResponse response = ProxyConsumer.consumeSingleMessage(context.getChannel(),
                                                                              context.getSink().getQueueName());

                    if (response == null) continue;

                    final Message msg = MessageFactory.createMessage(response);

                    if (msg == null) continue;

                    this.doUncompress(context, msg);

                    consumeMsgs.add(msg);
                }
            } catch (IOException e) {
                ExceptionHelper.logException(logger, e, "handle");
            } catch (RuntimeException e) {
                ExceptionHelper.logException(logger, e, "handle");
            }
        }
    }

    @Override
    public void process(MessageContext msgContext) {
        IMessageReceiveListener receiveListener = msgContext.getReceiveListener();
        receiveListener.onMessage(msgContext.getConsumeMsgs().get(0));
    }

    public static class ValidateEvent extends CarryEvent {
    }

    public static class PermissionCheckEvent extends CarryEvent {
    }

    public static class SyncConsumeEvent extends CarryEvent {
    }


}
