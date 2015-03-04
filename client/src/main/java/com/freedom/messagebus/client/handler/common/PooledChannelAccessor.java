package com.freedom.messagebus.client.handler.common;

import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.handler.IHandlerChain;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * pooled channel accessor used to access a channel from a pool
 * the pool based on apache common pool (v2.0)
 */
public class PooledChannelAccessor extends AbstractHandler {

    private static final Log logger = LogFactory.getLog(PooledChannelAccessor.class);

    /**
     * the main process method all sub class must implement
     *
     * @param context the message context
     * @param chain   the instance of IHandlerChain
     */
    @Override
    public void handle(final MessageContext context,
                       final IHandlerChain chain) {
//        final AbstractPool<Channel> pool = context.getPool();
//
//        context.setChannel(pool.getResource());
//
//        try {
//            if (context.getCarryType().equals(MessageCarryType.CONSUME) && !context.isSync()) {
//                try {
//                    context.getChannel().basicRecover();
//                } catch (IOException e) {
//                    logger.error("[handle] occurs a IOException : " + e.getMessage());
//                }
//            }
//
//            chain.handle(context);
//
//            //return resource
//            //if carry is consume or request then release current consumer
//            if (context.getCarryType().equals(MessageCarryType.CONSUME) && !context.isSync()) {
//                if (context.getConsumerTag() != null && !context.getConsumerTag().isEmpty()) {
//                    try {
//                        if (context.getChannel().isOpen() && context.getChannel().getConnection().isOpen())
//                            context.getChannel().basicCancel(context.getConsumerTag());
//                    } catch (IOException e) {
//                        logger.error("[destroy] occurs a IOException : " + e.getMessage());
//                    }
//                }
//            }
//        } finally {
//            pool.returnResource(context.getChannel());
//        }
    }
}
