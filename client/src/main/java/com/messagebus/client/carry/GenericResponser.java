package com.messagebus.client.carry;

import com.messagebus.client.IRequestListener;
import com.messagebus.client.MessageContext;
import com.messagebus.client.handler.MessageCarryHandlerChain;
import com.messagebus.client.model.MessageCarryType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class GenericResponser extends AbstractMessageCarryer implements Runnable, IResponser {

    private static final Log logger = LogFactory.getLog(GenericResponser.class);

    private Thread           currentThread;
    private String           secret;
    private IRequestListener onRequest;
    private TimeUnit         timeUnit;

    private       long      timeout     = 0;
    private final Lock      mainLock    = new ReentrantLock();
    private final Condition mainBlocker = mainLock.newCondition();

    public GenericResponser() {
        this.currentThread = new Thread(this);
        this.currentThread.setDaemon(true);
    }

    @Override
    public void response(String secret, IRequestListener onRequest, long timeout, TimeUnit timeUnit) {
        this.onRequest = onRequest;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.secret = secret;

        this.startup();
    }

    @Override
    public void run() {
        final MessageContext ctx = initMessageContext();
        ctx.setCarryType(MessageCarryType.RESPONSE);
        ctx.setSourceNode(this.getContext().getConfigManager().getNodeView(secret).getCurrentQueue());
        ctx.setRequestListener(this.onRequest);
        ctx.setNoticeListener(getContext().getNoticeListener());

        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.RESPONSE, this.getContext());
        this.handlerChain.handle(ctx);
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
