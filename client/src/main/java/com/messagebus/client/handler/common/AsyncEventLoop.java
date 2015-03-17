package com.messagebus.client.handler.common;

import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.common.ExceptionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * the receive event loop
 */
public class AsyncEventLoop implements Runnable {

    private static final Log logger = LogFactory.getLog(AsyncEventLoop.class);

    private MessageContext context;
    private IHandlerChain  chain;
    private Thread         currentThread;
    private boolean        isClosed;

    public AsyncEventLoop() {
        this.currentThread = new Thread(this);
        this.currentThread.setDaemon(true);
        this.currentThread.setName("async-event-loop-thread");
    }

    @Override
    public void run() {
        try {
            this.chain.handle(this.context);
        } catch (Exception e) {
            ExceptionHelper.logException(logger, e, "run");
        }

        logger.info("******** thread id " + this.getThreadID() + " quit from message receiver ********");
    }


    public void startEventLoop() {
        this.isClosed = false;
        this.currentThread.start();
    }

    /**
     * shut down launch a interrupt to itself
     */
    public void shutdown() {
        if (!this.isClosed) {
            this.currentThread.interrupt();
            this.isClosed = true;
        }
    }

    public boolean isAlive() {
        return this.currentThread.isAlive() && !this.isClosed;
    }

    protected long getThreadID() {
        return this.currentThread.getId();
    }

    public MessageContext getContext() {
        return context;
    }

    public void setContext(MessageContext context) {
        this.context = context;
    }

    public IHandlerChain getChain() {
        return chain;
    }

    public void setChain(IHandlerChain chain) {
        this.chain = chain;
    }

}
