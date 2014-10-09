package com.freedom.messagebus.scenario.client;


import com.freedom.messagebus.client.*;
import com.freedom.messagebus.common.message.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ResponseTemplate {

    private static final Log    logger = LogFactory.getLog(AsyncConsumeTemplate.class);
    private static final String appkey = "LAJFOWFALSKDJFALLKAJSDFLKSDFJLWKJ";

    private static final String host = "115.29.96.85";
    private static final int    port = 2181;

    public static void main(String[] args) {
        ResponseService service = new ResponseService();

        //launch!!!
        service.start();

        //blocking main-thread for seeing effect
        try {
            TimeUnit.SECONDS.sleep(60);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //stop the daemon server
        service.stopService();
    }

    public static class ResponseService extends Thread {

        Messagebus client = Messagebus.getInstance(appkey);

        String          appName        = "crm";
        IConsumerCloser consumerCloser = null;
        private final Object lockObj = new Object();

        @Override
        public void run() {
            try {
                synchronized (lockObj) {
                    //set zookeeper info
                    client.setZkHost(host);
                    client.setZkPort(port);

                    client.open();
                    IConsumer consumer = client.getConsumer();
                    final IResponser responser = client.getResponser();
                    consumerCloser = consumer.consume(appName, new IMessageReceiveListener() {
                        @Override
                        public void onMessage(Message message, IConsumerCloser consumerCloser) {
                            //handle message
                            String msgId = String.valueOf(message.getMessageHeader().getMessageId());
                            logger.info("[" + msgId +
                                            "]-[" + message.getMessageHeader().getType() + "]");

                            //send response
                            responser.responseTmpMessage(message, msgId);
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
                consumerCloser.closeConsumer();
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
