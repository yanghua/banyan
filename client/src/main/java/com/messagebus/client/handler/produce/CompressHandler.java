package com.messagebus.client.handler.produce;

import com.google.common.base.Strings;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.message.model.Message;
import com.messagebus.common.Constants;
import com.messagebus.common.compress.CompressorFactory;
import com.messagebus.common.compress.ICompressor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;

/**
 * compress handler
 */
public class CompressHandler extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(CompressHandler.class);

    @Override
    public void handle(MessageContext context, IHandlerChain chain) {
        String compressAlgo = context.getTargetNode().getCompress();
        if (!Strings.isNullOrEmpty(compressAlgo)) {
            ICompressor compressor = CompressorFactory.createCompressor(compressAlgo);
            if (compressor != null) {
                for (Message msg : context.getMessages()) {
                    if (msg.getHeaders() == null) msg.setHeaders(new HashMap<String, Object>(1));
                    msg.getHeaders().put(Constants.MESSAGE_HEADER_KEY_COMPRESS_ALGORITHM, compressAlgo);
                    msg.setContent(compressor.compress(msg.getContent()));
                }
            } else {
                logger.error("the target node with name : " + context.getTargetNode().getName()
                                 + " configured a compress named : " + compressAlgo
                                 + " but client can not get the compressor instance. ");
            }
        }


        chain.handle(context);
    }


}
