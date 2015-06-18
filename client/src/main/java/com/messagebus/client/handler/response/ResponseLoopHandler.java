package com.messagebus.client.handler.response;

import com.messagebus.client.IRequestListener;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.common.CommonLoopHandler;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.common.ExceptionHelper;
import com.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Created by yanghua on 3/16/15.
 */
public class ResponseLoopHandler extends CommonLoopHandler {

    private static final Log logger = LogFactory.getLog(ResponseLoopHandler.class);

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
            ExceptionHelper.logException(logger, e, "response process");
            throw new RuntimeException(e);
        }
    }
}
