package com.messagebus.client.handler.response;

import com.messagebus.client.IRequestListener;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.common.CommonLoopHandler;
import com.messagebus.client.message.model.IMessage;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.client.message.transfer.MsgBodyTransfer;
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
        IMessage requestMsg = msgContext.getConsumedMsg();
        String tempQueueName = requestMsg.getMessageHeader().getCorrelationId();
        msgContext.setTempQueueName(tempQueueName);
        IMessage respMsg = requestListener.onRequest(msgContext.getConsumedMsg());

        byte[] msgBody = MsgBodyTransfer.box(respMsg.getMessageBody());
        AMQP.BasicProperties properties = MessageHeaderTransfer.box(respMsg.getMessageHeader());
        try {
            ProxyProducer.produce("",
                                  msgContext.getChannel(),
                                  tempQueueName,
                                  msgBody,
                                  properties);
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "response process");
            throw new RuntimeException(e);
        }
    }
}
