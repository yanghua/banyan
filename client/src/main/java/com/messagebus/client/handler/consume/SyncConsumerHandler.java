package com.messagebus.client.handler.consume;

import com.google.common.base.Strings;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.common.ExceptionHelper;
import com.messagebus.common.compress.CompressorFactory;
import com.messagebus.common.compress.ICompressor;
import com.messagebus.interactor.proxy.ProxyConsumer;
import com.rabbitmq.client.GetResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class SyncConsumerHandler extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(SyncConsumerHandler.class);

    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        if (context.isSync()) {
            List<Message> consumeMsgs = new ArrayList<Message>(context.getConsumeMsgNum());
            context.setConsumeMsgs(consumeMsgs);
            try {
                int countDown = context.getConsumeMsgNum();
                while (countDown-- > 0) {
                    GetResponse response = ProxyConsumer.consumeSingleMessage(context.getChannel(),
                                                                              context.getSourceNode().getValue());

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

        chain.handle(context);
    }

    public void doUncompress(MessageContext context, Message receivedMsg) {
        String compressAlgo = context.getSourceNode().getCompress();
        if (!Strings.isNullOrEmpty(compressAlgo)) {
            ICompressor compressor = CompressorFactory.createCompressor(compressAlgo);
            if (compressor != null) {
                receivedMsg.setContent(compressor.uncompress(receivedMsg.getContent()));
            } else {
                logger.error("the target node with name : " + context.getTargetNode().getName()
                                 + " configured a compress named : " + compressAlgo
                                 + " but client can not get the compressor instance. ");
            }
        }
    }

}
