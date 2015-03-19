package com.messagebus.managesystem.service;

import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.MessagebusUnOpenException;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

public class MessagebusService {

    private static final Log logger = LogFactory.getLog(MessagebusService.class);
    private MessagebusSinglePool singlePool;
    private Messagebus client;

    public MessagebusService() {
        singlePool = new MessagebusSinglePool(Constants.PUBSUBER_HOST, Constants.PUBSUBER_PORT);
        client = singlePool.getResource();
    }

    public void produceMessage(String queueName, Message msg) {
        try {
//            client.produce(, queueName, msg);
        } catch (MessagebusUnOpenException e) {
            logger.error("[MessagebusUnOpenException] occurs a MessagebusUnOpenException : " + e.getMessage());
        } finally {
            singlePool.returnResource(client);
            singlePool.destroy();
        }
    }

    public void produceDBOperate(String cmd, String tbName) {
        Message operateMsg = (Message) MessageFactory.createMessage(MessageType.QueueMessage);
        Map<String, Object> headerMap = new HashMap<>(2);
        headerMap.put("COMMAND", cmd);
        headerMap.put("TABLE", tbName);
//        operateMsg.getMessageHeader().setHeaders(headerMap);
//        Message.MessageBody body = new Message.MessageBody();
//        body.setContent(new byte[0]);
//        operateMsg.setMessageBody(body);

        this.produceMessage(Constants.SERVER_QUEUE_NAME, operateMsg);
    }

}
