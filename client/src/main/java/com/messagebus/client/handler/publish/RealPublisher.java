package com.messagebus.client.handler.publish;

import com.google.common.base.Strings;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.transfer.MessageHeaderTransfer;
import com.messagebus.client.model.HandlerModel;
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

public class RealPublisher extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(RealPublisher.class);

    @Override
    public void init(HandlerModel handlerModel) {
        super.init(handlerModel);
    }

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        try {
            List<Node> publishNodes = (List<Node>) context.getOtherParams().get("publishList");
            for (Node node : publishNodes) {
                for (Message msg : context.getMessages()) {
                    //process message content (compress)
                    Message compressedMsg = this.doCompress(msg, node);
                    AMQP.BasicProperties properties = MessageHeaderTransfer.box(compressedMsg);

                    ProxyProducer.produce(Constants.PROXY_EXCHANGE_NAME,
                                          context.getChannel(),
                                          node.getRoutingKey(),
                                          compressedMsg.getContent(),
                                          properties);
                }
            }

            chain.handle(context);
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "handle");
            throw new RuntimeException(e);
        } catch (CloneNotSupportedException e) {
            ExceptionHelper.logException(logger, e, "handle");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    private Message doCompress(Message msg, Node Subscriber) throws CloneNotSupportedException {
        String compressAlgo = Subscriber.getCompress();
        if (!Strings.isNullOrEmpty(compressAlgo)) {
            ICompressor compressor = CompressorFactory.createCompressor(compressAlgo);
            if (compressor != null) {
                Message clonedMsg = (Message) msg.clone();
                if (clonedMsg.getHeaders() == null) clonedMsg.setHeaders(new HashMap<String, Object>(1));

                clonedMsg.getHeaders().put(Constants.MESSAGE_HEADER_KEY_COMPRESS_ALGORITHM, compressAlgo);
                clonedMsg.setContent(compressor.compress(clonedMsg.getContent()));

                return clonedMsg;
            } else {
                logger.error("the target subscriber with name : " + Subscriber.getName()
                                 + " configured a compress named : " + compressAlgo
                                 + " but client can not get the compressor instance. ");
            }
        }

        return msg;
    }

}
