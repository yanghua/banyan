package com.freedom.messagebus.scenario.client;

import com.freedom.messagebus.client.*;
import com.freedom.messagebus.client.message.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * asynchronous consume 的常用使用场景有如下特点：
 * (1)长时间被动等待的服务端处理程序/组件
 * (2)属于请求/响应模型的服务器端
 * (3)宿主环境下作为独立线程的后台处理程序
 */
public class AsyncConsumeTemplate {

    private static final Log    logger = LogFactory.getLog(AsyncConsumeTemplate.class);
    private static final String appid  = "LAJFOWFALSKDJFALLKAJSDFLKSDFJLWKJ";

    private static final String host = "172.16.206.30";
    private static final int    port = 2181;

    public static void main(String[] args) {
        ConsumerService service = new ConsumerService();

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

    public static class ConsumerService extends Thread {

        Messagebus client = Messagebus.createClient(appid);

        String          appName        = "file";
        IReceiverCloser consumerCloser = null;
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
                    consumerCloser = consumer.consume(appName, new IMessageReceiveListener() {
                        @Override
                        public void onMessage(Message message, IReceiverCloser consumerCloser) {
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
            } finally {
                logger.info("close client");
                if (consumerCloser != null)
                    consumerCloser.close();
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
