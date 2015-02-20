package com.freedom.messagebus.scenario.client;

import com.freedom.messagebus.client.*;
import com.freedom.messagebus.client.impl.AsyncConsumer;
import com.freedom.messagebus.client.message.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;

/**
 * asynchronous consume 的常用使用场景有如下特点：
 * (1)长时间被动等待的服务端处理程序/组件
 * (2)属于请求/响应模型的服务器端
 * (3)宿主环境下作为独立线程的后台处理程序
 */
public class AsyncConsumeTemplate {

    private static final Log    logger = LogFactory.getLog(AsyncConsumeTemplate.class);
    private static final String appid  = "djB5l1n7PbFsszF5817JOon2895El1KP";

    private static final String host = "127.0.0.1";
    private static final int    port = 6379;

    public static void main(String[] args) {
//        asyncConsume();
        asyncConsumeWithTimeout(10);
    }

    private static void asyncConsume() {
        Messagebus client = Messagebus.createClient(appid);
        //set zookeeper info
        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

        try {
            client.open();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        }

        String appName = "erp";
        AsyncConsumer asyncConsumer = client.getAsyncConsumer(appName, new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message, IReceiverCloser consumerCloser) {
                logger.info("[" + message.getMessageHeader().getMessageId() +
                                "]-[" + message.getMessageHeader().getType() + "]");
            }
        });

        asyncConsumer.startup();

        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        asyncConsumer.shutdown();
        client.close();
    }

    private static void asyncConsumeWithTimeout(long seconds) {
        Messagebus client = Messagebus.createClient(appid);
        //set zookeeper info
        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

        try {
            client.open();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        }

        String appName = "erp";
        AsyncConsumer asyncConsumer = client.getAsyncConsumer(appName, new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message, IReceiverCloser consumerCloser) {
                logger.info("[" + message.getMessageHeader().getMessageId() +
                                "]-[" + message.getMessageHeader().getType() + "]");
            }
        });

        asyncConsumer.setTimeout(seconds);
        asyncConsumer.setTimeUnit(TimeUnit.SECONDS);

        asyncConsumer.startup();

        client.close();
    }
}
