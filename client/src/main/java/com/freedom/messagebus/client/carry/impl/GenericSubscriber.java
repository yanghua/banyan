package com.freedom.messagebus.client.carry.impl;

import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.client.AbstractMessageCarryer;
import com.freedom.messagebus.client.IMessageReceiveListener;
import com.freedom.messagebus.client.MessageContext;
import com.freedom.messagebus.client.carry.ISubscriber;
import com.freedom.messagebus.client.handler.IHandlerChain;
import com.freedom.messagebus.client.handler.MessageCarryHandlerChain;
import com.freedom.messagebus.client.handler.common.AsyncEventLoop;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GenericSubscriber extends AbstractMessageCarryer implements Runnable, ISubscriber {

    private static final Log logger = LogFactory.getLog(GenericSubscriber.class);

    private Thread                  currentThread;
    private TimeUnit                timeUnit;
    private List<String>            subQueueNames;
    private IMessageReceiveListener onMessage;

    private       long      timeout      = 0;
    private final Lock      eventLocker  = new ReentrantLock();
    private final Condition eventBlocker = eventLocker.newCondition();
    private final Lock      mainLock     = new ReentrantLock();
    private final Condition mainBlocker  = mainLock.newCondition();

    public GenericSubscriber() {
    }

    @Override
    public void run() {
        eventLocker.lock();
        final MessageContext ctx = initMessageContext();
        try {
            ctx.setCarryType(MessageCarryType.SUBSCRIBE);
            //note: here we should change the node's name to nodename-erp pattern
            Node originalNode = this.getContext().getConfigManager()
                                    .getAppIdQueueMap().get(this.getContext().getAppId());
            String originalNodeName = originalNode.getName();
            String sourceNodeName = originalNodeName + Constants.PUBSUB_QUEUE_NAME_SUFFIX;
            Node sourceNode = this.getContext().getConfigManager()
                                  .getPubsubNodeMap().get(sourceNodeName);
            ctx.setSourceNode(sourceNode);
            ctx.setListener(this.onMessage);
            ctx.setSync(false);
            this.preProcessSubQueueNames(subQueueNames);
            ctx.setSubQueueNames(subQueueNames);

            checkState();

            this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.SUBSCRIBE, this.getContext());
            this.genericSubscribe(ctx, handlerChain);

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

    @Override
    public void subscribe(IMessageReceiveListener onMessage, List<String> subQueues, long timeout, TimeUnit unit) {
        this.onMessage = onMessage;
        this.subQueueNames = subQueues;
        this.timeout = timeout;
        this.timeUnit = unit;
        this.startup();
    }

    private void startup() {
        if (this.timeout != 0) {
            mainLock.lock();
            try {
                this.currentThread = new Thread(this);
                this.currentThread.setDaemon(true);
                this.currentThread.setName("subscriber-thread");
                this.currentThread.start();
                mainBlocker.await(this.timeout,
                                  this.timeUnit == null ? TimeUnit.SECONDS : this.timeUnit);
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
