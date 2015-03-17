package com.messagebus.scenario.client;

import com.messagebus.client.IRequestListener;
import com.messagebus.client.MessageResponseTimeoutException;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusConnectedFailedException;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.client.message.model.QueueMessage;
import com.messagebus.common.Constants;
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

        request();
    }

    private static void request() {
        String secret = "iuoqiwejicaoisfaisfbsqewnfjnfa";
        String token = "cakjdhfjasdflqjoiajsdjflqkuwef";
        Messagebus client = new Messagebus();
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

        Message responseMsg = null;

        try {
            responseMsg = client.request(secret, "emapDemoResponse", msg, token, 10);
        } catch (MessageResponseTimeoutException e) {
            e.printStackTrace();
        }

        client.close();

        if (responseMsg != null) {
            logger.info("received response message : " + responseMsg.getMessageHeader().getCorrelationId());
        }
    }

    private static void response() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                final String secret = "zxjhvclawenlkfhsladfnqpwenflak";
                final Messagebus client = new Messagebus();

                client.setPubsuberHost(host);
                client.setPubsuberPort(port);

                try {
                    client.open();

                    client.response(secret, new IRequestListener() {

                        @Override
                        public Message onRequest(Message requestMsg) {
                            logger.info("got requested message : " + requestMsg.getMessageHeader().getCorrelationId());

                            Message respMsg = MessageFactory.createMessage(MessageType.QueueMessage);
                            respMsg.getMessageHeader().setContentType("text/plain");
                            respMsg.getMessageHeader().setContentEncoding("utf-8");
                            respMsg.getMessageHeader().setCorrelationId(requestMsg.getMessageHeader().getCorrelationId());

                            QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
                            body.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));

                            respMsg.setMessageBody(body);

                            return respMsg;
                        }

                    }, 10, TimeUnit.SECONDS);

                    client.close();
                } catch (MessagebusConnectedFailedException e) {
                    e.printStackTrace();
                }
            }

        }).start();

    }

}
