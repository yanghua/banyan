package com.freedom.messagebus.client.impl;

import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.client.AbstractMessageCarryer;
import com.freedom.messagebus.client.IMessageReceiveListener;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.MessageCarryHandlerChain;
import com.freedom.messagebus.client.handler.common.AsyncEventLoop;
import com.freedom.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * a async consumer
 */
public class AsyncConsumer extends AbstractMessageCarryer implements Runnable {

    private static final Log logger = LogFactory.getLog(AsyncConsumer.class);

    private Thread                  currentThread;
    private IMessageReceiveListener onMessage;
    private String                  appName;

    private TimeUnit timeUnit;
    private       long      timeout      = 0;
    private final Lock      eventLocker  = new ReentrantLock();
    private final Condition eventBlocker = eventLocker.newCondition();
    private final Lock      mainLock     = new ReentrantLock();
    private final Condition mainBlocker  = mainLock.newCondition();

    public AsyncConsumer() {
        this.currentThread = new Thread(this);
        this.currentThread.setDaemon(true);
    }

    @Override
    public void run() {
        eventLocker.lock();
        final MessageContext ctx = new MessageContext();
        try {
            ctx.setCarryType(MessageCarryType.CONSUME);
            ctx.setAppId(this.getContext().getAppId());

            ctx.setSourceNode(ConfigManager.getInstance().getAppIdQueueMap().get(this.getContext().getAppId()));
            Node node = ConfigManager.getInstance().getQueueNodeMap().get(appName);
            ctx.setTargetNode(node);

            ctx.setPool(this.getContext().getPool());
            ctx.setConnection(this.getContext().getConnection());
            ctx.setListener(onMessage);
            ctx.setSync(false);
            super.setMsgContext(ctx);

            checkState();

            this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.CONSUME,
                                                             this.getContext());
            //async consume
            this.asyncConsume(ctx, handlerChain);

            if (this.timeout != 0) {
                eventBlocker.await(this.timeout,
                                   this.getTimeUnit() == null ? TimeUnit.SECONDS : this.getTimeUnit());
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

    public void startup() {
        if (this.timeout != 0) {
            mainLock.lock();

            try {
                this.currentThread.start();
                mainBlocker.await(this.timeout,
                                  this.getTimeUnit() == null ? TimeUnit.SECONDS : this.getTimeUnit());
            } catch (InterruptedException e) {

            } finally {
                this.shutdown();
                mainLock.unlock();
            }
        } else {
            this.currentThread.start();
        }

    }

    public void shutdown() {
        this.currentThread.interrupt();
    }

    public IMessageReceiveListener getOnMessage() {
        return onMessage;
    }

    public void setOnMessage(IMessageReceiveListener onMessage) {
        this.onMessage = onMessage;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    private void asyncConsume(MessageContext context,
                              IHandlerChain chain) {
        AsyncEventLoop eventLoop = new AsyncEventLoop();
        eventLoop.setChain(chain);
        eventLoop.setContext(context);
        context.setAsyncEventLoop(eventLoop);

        //repeat current handler
        if (chain instanceof MessageCarryHandlerChain) {
            eventLoop.startEventLoop();
        } else {
            throw new RuntimeException("the type of chain's instance is not MessageCarryHandlerChain");
        }
    }

}
