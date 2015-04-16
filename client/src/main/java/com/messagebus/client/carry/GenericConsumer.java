package com.messagebus.client.carry;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.MessageCarryHandlerChain;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * a async consumer
 */
class GenericConsumer extends AbstractMessageCarryer implements Runnable, IConsumer {

    private static final Log logger = LogFactory.getLog(GenericConsumer.class);

    private Thread                  currentThread;
    private String                  secret;
    private IMessageReceiveListener onMessage;
    private TimeUnit                timeUnit;

    private       long      timeout     = 0;
    private final Lock      mainLock    = new ReentrantLock();
    private final Condition mainBlocker = mainLock.newCondition();

    public GenericConsumer() {
    }

    @Override
    public void run() {
        final MessageContext ctx = initMessageContext();
        ctx.setSecret(this.secret);
        ctx.setCarryType(MessageCarryType.CONSUME);
        ctx.setSourceNode(this.getContext().getConfigManager().getSecretNodeMap().get(this.secret));
        ctx.setReceiveListener(onMessage);
        ctx.setSync(false);
        ctx.setNoticeListener(getContext().getNoticeListener());

        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.CONSUME, this.getContext());
        this.handlerChain.handle(ctx);
    }

    @Override
    public void consume(String secret, long timeout, TimeUnit unit, IMessageReceiveListener onMessage) {
        this.onMessage = onMessage;
        this.timeout = timeout;
        this.timeUnit = unit;
        this.secret = secret;
        this.currentThread = new Thread(this);
        this.currentThread.setDaemon(true);

        this.startup();
    }

    @Override
    public List<Message> consume(String secret, int expectedNum) {
        final MessageContext ctx = initMessageContext();
        ctx.setSecret(secret);
        ctx.setCarryType(MessageCarryType.CONSUME);
        ctx.setSourceNode(this.getContext().getConfigManager().getSecretNodeMap().get(secret));
        ctx.setConsumeMsgNum(expectedNum);
        ctx.setSync(true);

        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.CONSUME, this.getContext());
        //launch pipeline
        this.handlerChain.handle(ctx);

        return ctx.getConsumeMsgs();
    }

    public void shutdown() {
        this.currentThread.interrupt();
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

}
