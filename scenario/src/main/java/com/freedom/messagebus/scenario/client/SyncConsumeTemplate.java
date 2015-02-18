package com.freedom.messagebus.scenario.client;

import com.freedom.messagebus.client.*;
import com.freedom.messagebus.client.message.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * 同步&阻塞式的消费常用场景属于非服务型消费或者按序消费的模型
 */
public class SyncConsumeTemplate {

    private static final Log logger = LogFactory.getLog(AsyncConsumeTemplate.class);

    private static final String appId   = "6vifQNkw225U6dS8cI92rS2eS1o7ZehQ";            //ucp
    private static final String host    = "172.16.206.30";
    private static final int    port    = 2181;
    private static final String appName = "crm";

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
