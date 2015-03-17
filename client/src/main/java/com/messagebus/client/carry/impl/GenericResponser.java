package com.messagebus.client.carry.impl;

import com.messagebus.client.AbstractMessageCarryer;
import com.messagebus.client.IRequestListener;
import com.messagebus.client.MessageContext;
import com.messagebus.client.carry.IResponser;
import com.messagebus.client.handler.IHandlerChain;
import com.messagebus.client.handler.MessageCarryHandlerChain;
import com.messagebus.client.handler.common.AsyncEventLoop;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GenericResponser extends AbstractMessageCarryer implements Runnable, IResponser {

    private static final Log logger = LogFactory.getLog(GenericResponser.class);

    private Thread           currentThread;
    private String           secret;
    private IRequestListener onRequest;
    private TimeUnit         timeUnit;

    private       long      timeout      = 0;
    private final Lock      eventLocker  = new ReentrantLock();
    private final Condition eventBlocker = eventLocker.newCondition();
    private final Lock      mainLock     = new ReentrantLock();
    private final Condition mainBlocker  = mainLock.newCondition();

    public GenericResponser() {
        this.currentThread = new Thread(this);
        this.currentThread.setDaemon(true);
    }

    @Override
    public void response(String secret, IRequestListener requestListener, long timeout, TimeUnit timeUnit) {
        this.onRequest = requestListener;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.secret = secret;

        this.startup();
    }

    @Override
    public void run() {
        eventLocker.lock();
        final MessageContext ctx = initMessageContext();
        try {
            ctx.setCarryType(MessageCarryType.RESPONSE);
            ctx.setSourceNode(this.getContext().getConfigManager().getSecretNodeMap().get(this.secret));
            ctx.setRequestListener(this.onRequest);
            ctx.setNoticeListener(getContext().getNoticeListener());

            checkState();

            this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.RESPONSE, this.getContext());
            this.genericResponse(ctx, handlerChain);

            if (this.timeout != 0) {
                eventBlocker.await(this.timeout,
                                   this.timeUnit == null ? TimeUnit.SECONDS : this.timeUnit);
            } else {
                eventBlocker.await();
            }
        } catch (InterruptedException e) {
        } finally {
            if (ctx.getAsyncEventLoop().isAlive()) {
                ctx.getAsyncEventLoop().shutdown();
            }

            eventLocker.unlock();
        }
    }

    private void startup() {
        if (this.timeout != 0) {
            mainLock.lock();

            try {
                this.currentThread.start();
                mainBlocker.await(this.timeout,
                                  this.timeUnit == null ? TimeUnit.SECONDS : this.timeUnit);
            } catch (InterruptedException e) {

            } finally {
                this.currentThread.interrupt();
                mainLock.unlock();
            }
        } else {
            this.currentThread.start();
        }
    }

    private void genericResponse(MessageContext context, IHandlerChain chain) {
        AsyncEventLoop eventLoop = new AsyncEventLoop();
        eventLoop.setChain(chain);
        eventLoop.setContext(context);
        context.setAsyncEventLoop(eventLoop);

        if (chain instanceof MessageCarryHandlerChain) {
            eventLoop.startEventLoop();
        } else {
            throw new RuntimeException("the type of chain's instance is not MessageCarryHandlerChain");
        }
    }

}
