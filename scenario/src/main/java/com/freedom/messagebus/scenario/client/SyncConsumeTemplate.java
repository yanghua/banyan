package com.freedom.messagebus.scenario.client;

import com.freedom.messagebus.client.*;
import com.freedom.messagebus.client.impl.SyncConsumer;
import com.freedom.messagebus.client.message.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * 同步&阻塞式的消费常用场景属于非服务型消费或者按序消费的模型
 */
public class SyncConsumeTemplate {

    private static final Log logger = LogFactory.getLog(AsyncConsumeTemplate.class);

    private static final String appId   = "djB5l1n7PbFsszF5817JOon2895El1KP";            //ucp
    private static final String host    = "127.0.0.1";
    private static final int    port    = 6379;
    private static final String appName = "erp";

    public static void main(String[] args) {
        Messagebus client = Messagebus.createClient(appId);
        client.setPubsuberHost(host);
        client.setPubsuberPort(port);

        try {
            client.open();
            SyncConsumer syncConsumer = client.getSyncConsumer();
            List<Message> msgs = syncConsumer.consume(appName, 2);

            client.close();

            for (Message msg : msgs) {
                logger.info("message id : " + msg.getMessageHeader().getMessageId());
            }
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        } catch (MessagebusUnOpenException e) {
            e.printStackTrace();
        }
    }

}
