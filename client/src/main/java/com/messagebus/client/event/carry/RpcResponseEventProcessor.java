package com.messagebus.client.event.carry;

import com.google.common.eventbus.Subscribe;
import com.messagebus.client.MessageContext;
import com.messagebus.common.ExceptionHelper;
import com.rabbitmq.tools.jsonrpc.JsonRpcServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.*;

/**
 * Created by yanghua on 6/29/15.
 */
public class RpcResponseEventProcessor extends CommonEventProcessor {

    private static final Log logger = LogFactory.getLog(RpcResponseEventProcessor.class);

    public RpcResponseEventProcessor() {
    }

    @Subscribe
    public void onRpcResponse(RpcResponseEvent event) {
        super.exceptionCheck(event);
        logger.debug("=-=-=- event : onRpcResponse =-=-=-");
        MessageContext  context           = event.getMessageContext();
        ExecutorService executor          = Executors.newFixedThreadPool(1);
        Future<?>       rpcServerLoopTask = executor.submit(new RpcServerLoopTask(context));

        try {
            rpcServerLoopTask.get(context.getTimeout(), context.getTimeoutUnit());
        } catch (InterruptedException e) {
            logger.info(" rpc server interrupted!");
        } catch (ExecutionException e) {
            logger.error(e);
            event.getMessageContext().setThrowable(e);
        } catch (TimeoutException e) {
            logger.info("message loop timeout after : "
                    + context.getTimeout() + " [" + context.getTimeoutUnit() + "]");
            event.getMessageContext().setThrowable(e);
        }
    }

    @Override
    public void process(MessageContext msgContext) {
        throw new UnsupportedOperationException("this method should be implemented in consume event processor!");
    }

    public static class RpcResponseEvent extends CarryEvent {
    }

    /**
     * inner class : rpc server loop task
     */
    private class RpcServerLoopTask implements Runnable {

        private MessageContext msgContext;

        public RpcServerLoopTask(MessageContext msgContext) {
            this.msgContext = msgContext;
        }

        @Override
        public void run() {
            Class<?>      clazzOfInterface = (Class<?>) msgContext.getOtherParams().get("clazzOfInterface");
            Object        obj              = msgContext.getOtherParams().get("serviceProvider");
            JsonRpcServer server           = null;
            try {
                server = new JsonRpcServer(msgContext.getChannel(),
                        msgContext.getSink().getQueueName(),
                        clazzOfInterface,
                        obj);
                server.mainloop();
            } catch (IOException e) {
                ExceptionHelper.logException(logger, e, "real rpc responser");
                throw new RuntimeException(e);
            } finally {
                if (server != null) {
                    server.terminateMainloop();
                }
            }
        }

    }

}
