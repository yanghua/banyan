package com.messagebus.client.carry;

import com.messagebus.client.IRpcMessageProcessor;
import com.messagebus.client.MessageContext;
import com.messagebus.client.WrappedRpcServer;
import com.messagebus.client.handler.MessageCarryHandlerChain;
import com.messagebus.client.model.MessageCarryType;
import com.messagebus.client.model.Node;
import com.messagebus.common.ExceptionHelper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.RpcServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by yanghua on 4/8/15.
 */
public class GenericRpcResponser extends AbstractMessageCarryer implements IRpcResponser, Runnable {

    private static final Log logger = LogFactory.getLog(GenericRpcResponser.class);

    private Thread   currentThread;
    private String   secret;
    private Class<?> clazzOfInterface;
    private Object   serviceProvider;
    private TimeUnit timeUnit;

    private       long      timeout     = 0;
    private final Lock      mainLock    = new ReentrantLock();
    private final Condition mainBlocker = mainLock.newCondition();

    public GenericRpcResponser() {
    }

    @Override
    public void run() {
        MessageContext ctx = initMessageContext();
        ctx.setSecret(this.secret);
        ctx.setCarryType(MessageCarryType.RPCRESPONSE);
        ctx.setSourceNode(this.getContext().getConfigManager().getNodeView(secret).getCurrentQueue());
        Map<String, Object> otherParams = ctx.getOtherParams();
        otherParams.put("serviceProvider", this.serviceProvider);
        otherParams.put("clazzOfInterface", this.clazzOfInterface);
        ctx.setNoticeListener(getContext().getNoticeListener());

        checkState();

        this.handlerChain = new MessageCarryHandlerChain(MessageCarryType.RPCRESPONSE, this.getContext());
        this.handlerChain.handle(ctx);
    }

    @Override
    public void callback(String secret, Class<?> clazzOfInterface, Object serviceProvider, long timeout, TimeUnit timeUnit) {
        this.secret = secret;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.clazzOfInterface = clazzOfInterface;
        this.serviceProvider = serviceProvider;
        this.currentThread = new Thread(this);
        this.currentThread.setDaemon(true);

        this.startup();
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

    public void shutdown() {
        this.currentThread.interrupt();
    }

    @Override
    public WrappedRpcServer buildRpcServer(String secret, final IRpcMessageProcessor rpcMsgProcessor) {
        Node source = this.getContext().getConfigManager().getNodeView(secret).getCurrentQueue();
        try {
            RpcServer aServer = new RpcServer(this.getContext().getChannel(), source.getValue()) {

                @Override
                public byte[] handleCall(QueueingConsumer.Delivery request, AMQP.BasicProperties replyProperties) {
                    return rpcMsgProcessor.onRpcMessage(request.getBody());
                }

            };

            Constructor<WrappedRpcServer> rpcServerConstructor = WrappedRpcServer.class.getDeclaredConstructor(RpcServer.class);
            rpcServerConstructor.setAccessible(true);
            WrappedRpcServer wrappedRpcServer = rpcServerConstructor.newInstance(aServer);
            rpcServerConstructor.setAccessible(false);

            return wrappedRpcServer;
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "buildRpcServer");
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            ExceptionHelper.logException(logger, e, "buildRpcServer");
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            ExceptionHelper.logException(logger, e, "buildRpcServer");
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            ExceptionHelper.logException(logger, e, "buildRpcServer");
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            ExceptionHelper.logException(logger, e, "buildRpcServer");
            throw new RuntimeException(e);
        } catch (Exception e) {
            ExceptionHelper.logException(logger, e, "buildRpcServer");
            throw new RuntimeException(e);
        }
    }
}
