package com.messagebus.client.carry;

import com.messagebus.business.model.Node;
import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.MessageCarryHandlerChain;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class GenericSubscriber extends AbstractMessageCarryer implements Runnable, ISubscriber {

    private static final Log logger = LogFactory.getLog(GenericSubscriber.class);

    private Thread                  currentThread;
    private TimeUnit                timeUnit;
    private IMessageReceiveListener onMessage;
    private String                  secret;

    private       long      timeout     = 0;
    private final Lock      mainLock    = new ReentrantLock();
    private final Condition mainBlocker = mainLock.newCondition();

    public GenericSubscriber() {
    }

    @Override
    public void run() {
        final MessageContext ctx = initMessageContext();
        ctx.setCarryType(MessageCarryType.SUBSCRIBE);
        ctx.setSecret(this.secret);
        Node sourceNode = this.getContext().getConfigManager().getSecretNodeMap().get(this.secret);
        ctx.setSourceNode(sourceNode);
        ctx.setReceiveListener(this.onMessage);
        ctx.setSync(false);
        ctx.setNoticeListener(this.getContext().getNoticeListener());

        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.SUBSCRIBE, this.getContext());
        this.handlerChain.handle(ctx);
    }

    @Override
    public void subscribe(String secret, IMessageReceiveListener onMessage, long timeout, TimeUnit unit) {
        this.onMessage = onMessage;
        this.timeout = timeout;
        this.timeUnit = unit;
        this.secret = secret;

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

}
