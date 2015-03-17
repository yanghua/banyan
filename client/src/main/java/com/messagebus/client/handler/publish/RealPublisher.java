package com.messagebus.client.handler.publish;

import com.messagebus.business.model.Node;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.transfer.IMessageBodyTransfer;
import com.messagebus.client.message.transfer.MessageBodyTransferFactory;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.client.model.HandlerModel;
import com.messagebus.common.Constants;
import com.messagebus.common.ExceptionHelper;
import com.messagebus.interactor.proxy.ProxyProducer;
import com.rabbitmq.client.AMQP;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.List;

public class RealPublisher extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(RealPublisher.class);

    @Override
    public void init(HandlerModel handlerModel) {
        super.init(handlerModel);
    }

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        try {
            for (Message msg : context.getMessages()) {
                IMessageBodyTransfer msgBodyProcessor = MessageBodyTransferFactory.createMsgBodyProcessor(msg.getMessageType());
                byte[] msgBody = msgBodyProcessor.box(msg.getMessageBody());
                AMQP.BasicProperties properties = MessageHeaderTransfer.box(msg.getMessageHeader());

                List<Node> publishNodes = (List<Node>) context.getOtherParams().get("publishList");
                for (Node node : publishNodes) {
                    ProxyProducer.produce(Constants.PROXY_EXCHANGE_NAME,
                                          context.getChannel(),
                                          node.getRoutingKey(),
                                          msgBody,
                                          properties);
                }
            }

            chain.handle(context);
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "handle");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
