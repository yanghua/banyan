package com.freedom.messagebus.scenario.client;

import com.freedom.messagebus.client.*;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.model.MessageFactory;
import com.freedom.messagebus.client.message.model.MessageType;
import com.freedom.messagebus.client.message.model.QueueMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RequestTemplate {

    private static final Log    logger = LogFactory.getLog(RequestTemplate.class);
    private static final String appId  = "LAJFOWFALSKDJFALLKAJSDFLKSDFJLWKJ";
    private static final String host   = "115.29.96.85";
    private static final int    port   = 2181;

    public static void main(String[] args) {
        Messagebus messagebus = Messagebus.createClient(appId);
        messagebus.setPubsuberHost(host);
        messagebus.setPubsuberPort(port);

        String queueName = "crm";

        Message msg = MessageFactory.createMessage(MessageType.QueueMessage);
        msg.getMessageHeader().setReplyTo(queueName);
        msg.getMessageHeader().setContentType("text/plain");
        msg.getMessageHeader().setContentEncoding("utf-8");

        QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
        body.setContent("test".getBytes());
        msg.setMessageBody(body);

        Message respMsg = null;

        try {
            messagebus.open();
            IRequester requester = messagebus.getRequester();

            respMsg = requester.request(msg, queueName, 30000);
            //use response message...
            logger.info("response message : [" + respMsg.getMessageHeader().getMessageId() + "]");
        } catch (MessagebusConnectedFailedException | MessagebusUnOpenException |
            MessageResponseTimeoutException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            messagebus.close();
        }
    }

}
