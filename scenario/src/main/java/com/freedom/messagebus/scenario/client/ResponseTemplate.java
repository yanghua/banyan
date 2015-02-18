package com.freedom.messagebus.scenario.client;


import com.freedom.messagebus.client.*;
import com.freedom.messagebus.client.message.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ResponseTemplate {

    private static final Log    logger = LogFactory.getLog(AsyncConsumeTemplate.class);
    private static final String appid  = "LAJFOWFALSKDJFALLKAJSDFLKSDFJLWKJ";

    private static final String host = "172.16.206.30";
    private static final int    port = 2181;

    public static void main(String[] args) {
        responseUsage();
    }

    private static void responseUsage() {
        Messagebus client = Messagebus.createClient(appid);

        String appName = "server";

        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

        try {
            client.open();
            final IResponser responser = client.getResponser();

            AsyncConsumer asyncConsumer = client.getAsyncConsumer(
                appName,
                new IMessageReceiveListener() {
                    @Override
                    public void onMessage(Message message, IReceiverCloser consumerCloser) {
                        //handle message
                        String msgId = String.valueOf(message.getMessageHeader().getMessageId());
                        logger.info("[" + msgId +
                                        "]-[" + message.getMessageHeader().getType() + "]");

                        //send response
                        responser.responseTmpMessage(message, msgId);
                    }
                });

            asyncConsumer.startup();

            TimeUnit.SECONDS.sleep(30);

            asyncConsumer.shutdown();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
