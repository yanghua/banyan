package com.messagebus.client.event.carry;

import com.google.common.eventbus.Subscribe;
import com.messagebus.client.ConfigManager;
import com.messagebus.client.IRequestListener;
import com.messagebus.client.MessageContext;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.client.model.MessageCarryType;
import com.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Created by yanghua on 6/29/15.
 */
public class ResponseEventProcessor extends CommonEventProcessor {

    private static final Log logger = LogFactory.getLog(ResponseEventProcessor.class);

    public ResponseEventProcessor() {
    }

    @Subscribe
    public void onValidate(ValidateEvent event) {
        logger.debug("=-=-=- event : onValidate =-=-=-");
        super.onValidate(event);
        MessageContext context = event.getMessageContext();
        if (!context.getCarryType().equals(MessageCarryType.RESPONSE)) {
            logger.error(" message carry type should be response ");
            throw new RuntimeException("message carry type should be response ");
        }
    }

    @Subscribe
    public void onPermissionCheck(PermissionCheckEvent event) {
        logger.debug("=-=-=- event : onPermissionCheck =-=-=-");
        MessageContext context = event.getMessageContext();
        ConfigManager.Sink sink = context.getSink();
        boolean hasPermission = MessageCarryType.lookup(sink.getType()).equals(MessageCarryType.RESPONSE);

        if (!hasPermission) {
            logger.error("permission error : can not response. " +
                             "may be communicate type is wrong. " +
                             "current secret is : " + sink.getSecret());
            throw new RuntimeException("permission error : can not response. " +
                                           "may be communicate type is wrong. " +
                                           "current secret is : " + sink.getSecret());
        }
    }

    @Override
    public void process(MessageContext msgContext) {
        IRequestListener requestListener = msgContext.getRequestListener();
        Message requestMsg = msgContext.getConsumeMsgs().get(0);
        String tempQueueName = requestMsg.getCorrelationId();
        msgContext.setTempQueueName(tempQueueName);
        Message respMsg = requestListener.onRequest(msgContext.getConsumeMsgs().get(0));

        AMQP.BasicProperties properties = MessageHeaderTransfer.box(respMsg);
        try {
            ProxyProducer.produce("",
                                  msgContext.getChannel(),
                                  tempQueueName,
                                  respMsg.getContent(),
                                  properties);
        } catch (IOException e) {
            logger.error(" occurs a IOException : ", e);
            throw new RuntimeException(e);
        }
    }

    public static class ValidateEvent extends CarryEvent {
    }

    public static class PermissionCheckEvent extends CarryEvent {
    }


}
