package com.freedom.messagebus.client;

import com.freedom.messagebus.common.ExceptionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * a async consumer
 */
public class AsyncConsumer implements Runnable {

    private static final Log logger = LogFactory.getLog(AsyncConsumer.class);

    private IConsumer               consumer;
    private Thread                  currentThread;
    private IMessageReceiveListener onMessage;
    private String                  appName;
    private IReceiverCloser         consumerCloser;
    private final Lock      locker  = new ReentrantLock();
    private final Condition blocker = locker.newCondition();

    public AsyncConsumer(String appName,
                         IMessageReceiveListener onMessage,
                         IConsumer consumer) {
        this.currentThread = new Thread(this);
        this.currentThread.setDaemon(true);
        this.appName = appName;
        this.onMessage = onMessage;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        locker.lock();
        try {
            consumerCloser = this.consumer.consume(this.appName, this.onMessage);
            blocker.await();
        } catch (InterruptedException e) {

        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "AsyncConsumer");
        } finally {
            if (consumerCloser != null) {
                consumerCloser.close();
            }
            locker.unlock();
        }
    }

    public void startup() {
        this.currentThread.start();
    }

    public void shutdown() {
        this.currentThread.interrupt();
    }

}
