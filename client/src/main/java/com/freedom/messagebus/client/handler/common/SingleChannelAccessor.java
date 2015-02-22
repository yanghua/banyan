package com.freedom.messagebus.client.handler.common;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.rabbitmq.client.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * single channel accessor, it's a on hand channel-accessor
 * when it is unuseful, remember to close it
 */
public class SingleChannelAccessor extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(SingleChannelAccessor.class);


    private Channel channel;

    private boolean isInited = false;

    private void init(MessageContext context) {
        try {
            this.channel = context.getConnection().createChannel();
            this.isInited = true;
        } catch (IOException e) {
            logger.error("[init] occurs a IOException : " + e.getMessage());
            this.isInited = false;
        }
    }

    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    @Override
    public void handle(MessageContext context,
                       IHandlerChain chain) {
//        this.init(context);
//        if (!this.isInited) {
//            logger.error("[handle]: the [init] method invoked failed.");
//        }
//
//        context.setChannel(this.channel);
//        context.setDestroyer(new IChannelDestroyer() {
//            @Override
//            public void destroy(Channel channel) {
//                try {
//                    if (channel.isOpen())
//                        channel.close();
//                } catch (IOException e) {
//                    logger.error("[destroy] occurs a IOException : " + e.getMessage());
//                }
//            }
//        });
//
//        if (context.getCarryType().equals(MessageCarryType.CONSUME) && !context.isSync()) {
//            try {
//                context.getChannel().basicRecover();
//            } catch (IOException e) {
//                logger.error("[handle] occurs a IOException : " + e.getMessage());
//            }
//        }
//
//        chain.handle(context);
    }
}
