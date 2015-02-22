package com.freedom.messagebus.scenario.client;

import com.freedom.messagebus.client.*;
import com.freedom.messagebus.client.message.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class SubscribeTemplate {

    private static final Log    logger = LogFactory.getLog(SubscribeTemplate.class);
    private static final String appId  = "LAJFOWFALSKDJFALLKAJSDFLKSDFJLWKJ";

    private static final String host = "115.29.96.85";
    private static final int    port = 2181;

    public static void main(String[] args) {
        SubscribeService service = new SubscribeService();

        service.start();

        //blocking main-thread for seeing effect
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //stop the daemon server
        service.stopService();
    }

    public static class SubscribeService extends Thread {

        Messagebus client = Messagebus.createClient(appId);

        List<String>      subQueueNames    = new CopyOnWriteArrayList<>(new String[]{"crm"});
        ISubscribeManager subscribeManager = null;
        final Object lockObj = new Object();

        @Override
        public void run() {
            try {
                synchronized (lockObj) {
                    //set zookeeper info
                    client.setPubsuberHost(host);
                    client.setPubsuberPort(port);

                    client.open();
                    ISubscriber subscriber = client.getSubscriber();
                    subscribeManager = subscriber.subscribe(subQueueNames, new IMessageReceiveListener() {
                        @Override
                        public void onMessage(Message message) {
                            logger.info("[" + message.getMessageHeader().getMessageId() +
                                            "]-[" + message.getMessageHeader().getType() + "]");
                        }
                    });

                    logger.info("blocked for receiving message!");
                    lockObj.wait(0);
                    logger.info("released object lock!");
                }
            } catch (IOException | MessagebusUnOpenException |
                MessagebusConnectedFailedException | InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                logger.info("close client");
                subscribeManager.close();
                client.close();
            }
        }

        public void stopService() {
            //style 1 : use lock released
            synchronized (lockObj) {
                lockObj.notifyAll();
            }

            //style 2 : use interrupt
//            this.interrupt();
        }

    }

}
