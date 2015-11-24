package com.messagebus.client.event.carry;

import com.google.common.eventbus.Subscribe;
import com.messagebus.client.ConfigManager;
import com.messagebus.client.MessageContext;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.client.model.MessageCarryType;
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
        ConfigManager.Source source = context.getSource();
        if (!context.getCarryType().equals(MessageCarryType.PRODUCE)) {
            logger.error("message carry type should be produce, but current is : "
                             + context.getCarryType()
                             + "; node name : " + source.getName()
                             + "; node secret is : " + source.getSecret());
            throw new RuntimeException("message carry type should be produce, but current is : "
                                           + context.getCarryType()
                                           + "; node name : " + source.getName()
                                           + "; node secret is : " + source.getSecret());
        }

        if (context.getSink() == null) {
            logger.error("target node can not be null "
                             + "; node name : " + source.getName()
                             + "; node secret is : " + source.getSecret());
            throw new RuntimeException("target node can not be null "
                                           + "; node name : " + source.getName()
                                           + "; node secret is : " + source.getSecret());
        }

        if (!MessageCarryType.lookup(context.getSink().getType()).equals(MessageCarryType.CONSUME)) {
            logger.error("target node's type is illegal , "
                             + "target node name : " + context.getSink().getName());
            throw new RuntimeException("target node's type is illegal , "
                                           + "target node name : " + context.getSink().getName());
        }

        this.validateMessagesProperties(context);
    }

    @Subscribe
    public void onPermissionCheckEvent(PermissionCheckEvent event) {
        logger.debug("=-=-=- event : onPermissionCheckEvent =-=-=-");
        MessageContext context = event.getMessageContext();
        ConfigManager.Source source = context.getSource();
        ConfigManager.Sink sink = context.getSink();

        boolean hasPermission = false;

        ConfigManager.Stream stream = context.getStream();
        hasPermission = stream.getSourceName().equals(source.getName())
            && stream.getSinkSecret().equals(sink.getSecret());

        if (!hasPermission) {
            logger.error("[handle] can not produce message from queue [" + source.getName() +
                             "] to queue [" + sink.getName() + "]");
            throw new RuntimeException("can not produce message from queue [" + source.getName() +
                                           "] to queue [" + sink.getName() + "]");
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
                                                context.getSink().getRoutingKey(),
                                                msg.getContent(),
                                                properties);
                }
            } else {
                for (Message msg : context.getMessages()) {
                    AMQP.BasicProperties properties = MessageHeaderTransfer.box(msg);

                    ProxyProducer.produce(Constants.PROXY_EXCHANGE_NAME,
                                          context.getChannel(),
                                          context.getSink().getRoutingKey(),
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
