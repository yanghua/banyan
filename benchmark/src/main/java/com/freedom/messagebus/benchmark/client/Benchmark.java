package com.freedom.messagebus.benchmark.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;

public abstract class Benchmark {

    private static final Log logger = LogFactory.getLog(Benchmark.class);

    public void test(Runnable testTask, int holdTime, int fetchNum, String fileName) {
        logger.info("test begin");

        int fetchInterval = holdTime / fetchNum;
        long[] xArr = new long[fetchNum];
        long[] yArr = new long[fetchNum];

        //initialize value
        xArr[0] = 0;
        yArr[0] = 0;

        Thread testThread = new Thread(testTask);
        testThread.setDaemon(true);
        testThread.start();

        try {
            for (int i = 1; i < fetchNum; i++) {
                TimeUnit.MILLISECONDS.sleep(fetchInterval);

                xArr[i] = i * fetchInterval;
                yArr[i] = ((IFetcher) testTask).fetch();
            }

            ((ITerminater) testTask).terminate();

            //write to report file
            TestUtility.writeFile(fileName, xArr, yArr);
        } catch (InterruptedException e) {

        }

        logger.info("test end");
    }

}
