package com.freedom.messagebus.scenario.client;

import com.freedom.messagebus.client.*;
import com.freedom.messagebus.client.impl.AsyncConsumer;
import com.freedom.messagebus.client.message.model.*;
import com.freedom.messagebus.common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 2/24/15.
 */
public class RequestResponse {

    private static final Log logger = LogFactory.getLog(RequestResponse.class);

    private static final String host = "127.0.0.1";
    private static final int    port = 6379;

    public static void main(String[] args) {
        response();

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        request();
    }

    private static void request() {
        //crm
        String appid = "djB5l1n7PbFsszF5817JOon2895El1KP";
        Messagebus client = Messagebus.createClient(appid);
        //set zookeeper info
        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

        try {
            client.open();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        }

        Message msg = MessageFactory.createMessage(MessageType.QueueMessage);
        msg.getMessageHeader().setContentType("text/plain");
        msg.getMessageHeader().setContentEncoding("utf-8");

        QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
        body.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));

        msg.setMessageBody(body);

        IRequester requester = client.getRequester();

        Message responseMsg = null;

        try {
            responseMsg = requester.request(msg, "erp", 20);
        } catch (MessageResponseTimeoutException e) {
            e.printStackTrace();
        }

        client.close();

        if (responseMsg != null) {
            logger.info("received response message : " + responseMsg.getMessageHeader().getMessageId());
        }
    }

    private static void response() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                //erp
                String appid = "D0fW8u2u1v7S1IvI8qoQg3dUlLL5b36q";
                Messagebus client = Messagebus.createClient(appid);

                client.setPubsuberHost(host);
                client.setPubsuberPort(port);

                try {
                    client.open();
                    final IResponser responser = client.getResponser();

                    AsyncConsumer asyncConsumer = client.getAsyncConsumer(
                        new IMessageReceiveListener() {
                            @Override
                            public void onMessage(Message message) {
                                //handle message
                                String msgId = String.valueOf(message.getMessageHeader().getMessageId());
                                logger.info("[" + msgId +
                                                "]-[" + message.getMessageHeader().getType() + "]");

                                //send response
                                responser.responseTmpMessage(message, msgId);
                            }
                        });

                    asyncConsumer.setTimeout(30);
                    asyncConsumer.startup();

                    client.close();
                } catch (MessagebusConnectedFailedException e) {
                    e.printStackTrace();
                }
            }

        }).start();

    }

}
