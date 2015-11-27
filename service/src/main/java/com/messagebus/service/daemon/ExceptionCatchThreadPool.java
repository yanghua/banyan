package com.messagebus.service.daemon;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExceptionCatchThreadPool extends ThreadPoolExecutor {

    private static final Log logger = LogFactory.getLog(ExceptionCatchThreadPool.class);

    public ExceptionCatchThreadPool(int nThreads) {
        super(nThreads,
              nThreads,
              0L,
              TimeUnit.MILLISECONDS,
              new LinkedBlockingQueue<Runnable>());
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);

        if (t != null) {
            logger.error(t);
        }
    }
}
