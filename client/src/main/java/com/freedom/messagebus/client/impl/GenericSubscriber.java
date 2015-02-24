package com.freedom.messagebus.client.impl;

import com.freedom.messagebus.business.model.Config;
import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.client.*;
import com.freedom.messagebus.client.core.config.ConfigManager;
import com.freedom.messagebus.client.core.pool.AbstractPool;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.MessageCarryHandlerChain;
import com.freedom.messagebus.client.handler.common.AsyncEventLoop;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.Constants;
import com.rabbitmq.client.Channel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GenericSubscriber extends AbstractMessageCarryer implements Runnable {

    private static final Log logger = LogFactory.getLog(GenericSubscriber.class);

    private static volatile GenericSubscriber instance;
    private                 Channel           channel;
    private                 Thread            currentThread;
    private TimeUnit timeUnit;

    private       long      timeout      = 0;
    private final Lock      eventLocker  = new ReentrantLock();
    private final Condition eventBlocker = eventLocker.newCondition();
    private final Lock      mainLock     = new ReentrantLock();
    private final Condition mainBlocker = mainLock.newCondition();
    private List<String> subQueueNames;
    private IMessageReceiveListener onMessage;

    private GenericSubscriber(AbstractPool<Channel> pool) {
        this.channel = pool.getResource();
    }

    public static GenericSubscriber defaultSubscriber(AbstractPool<Channel> pool) {
        synchronized (GenericSubscriber.class) {
            if (instance == null) {
                synchronized (GenericSubscriber.class) {
                    instance = new GenericSubscriber(pool);
                }
            }
        }

        return instance;
    }

    @Override
    public void run() {
        eventLocker.lock();
        final MessageContext ctx = new MessageContext();
        try {
            ctx.setCarryType(MessageCarryType.SUBSCRIBE);
            ctx.setAppId(this.getContext().getAppId());
            //note: here we should change the node's name to nodename-erp pattern
            Node originalNode = ConfigManager.getInstance()
                                             .getAppIdQueueMap().get(this.getContext().getAppId());
            String originalNodeName = originalNode.getName();
            String sourceNodeName = originalNodeName+Constants.PUBSUB_QUEUE_NAME_SUFFIX;
            Node sourceNode = ConfigManager.getInstance().getPubsubNodeMap().get(sourceNodeName);
            ctx.setSourceNode(sourceNode);

            ctx.setChannel(this.channel);

            ctx.setPool(this.getContext().getPool());
            ctx.setConnection(this.getContext().getConnection());
            ctx.setListener(this.getOnMessage());
            ctx.setSync(false);

            this.preProcessSubQueueNames(subQueueNames);
            ctx.setSubQueueNames(subQueueNames);

            checkState();

            this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.SUBSCRIBE,
                                                             this.getContext());

            //consume
            this.genericSubscribe(ctx, handlerChain);

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
                this.currentThread = new Thread(this);
                this.currentThread.setDaemon(true);
                this.currentThread.start();
                mainBlocker.await(this.timeout,
                                  this.getTimeUnit() == null ? TimeUnit.SECONDS : this.getTimeUnit());
            } catch (InterruptedException e) {

            } finally {
                this.shutdown();
                mainLock.unlock();
            }
        } else {
            this.currentThread = new Thread(this);
            this.currentThread.setDaemon(true);
            this.currentThread.start();
        }
    }

    public void shutdown() {
        this.currentThread.interrupt();
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public List<String> getSubQueueNames() {
        return subQueueNames;
    }

    public void setSubQueueNames(List<String> subQueueNames) {
        this.subQueueNames = subQueueNames;
    }

    public IMessageReceiveListener getOnMessage() {
        return onMessage;
    }

    public void setOnMessage(IMessageReceiveListener onMessage) {
        this.onMessage = onMessage;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    private void preProcessSubQueueNames(List<String> subQueueNames) {
        for (int i = 0; i < subQueueNames.size(); i++) {
            String subQueueName = subQueueNames.get(i);
            if (subQueueName.endsWith(Constants.PUBSUB_QUEUE_NAME_SUFFIX))
                subQueueNames.set(i, subQueueName.replaceAll(Constants.PUBSUB_QUEUE_NAME_SUFFIX, ""));
        }
    }

    private void genericSubscribe(MessageContext context,
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
