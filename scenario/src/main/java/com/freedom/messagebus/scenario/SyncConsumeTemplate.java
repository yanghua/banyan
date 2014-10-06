package com.freedom.messagebus.scenario;

import com.freedom.messagebus.client.IConsumer;
import com.freedom.messagebus.client.Messagebus;
import com.freedom.messagebus.client.MessagebusConnectedFailedException;
import com.freedom.messagebus.client.MessagebusUnOpenException;
import com.freedom.messagebus.common.message.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * 同步&阻塞式的消费常用场景属于非服务型消费或者按序消费的模型
 */
public class SyncConsumeTemplate {

    private static final Log    logger = LogFactory.getLog(AsyncConsumeTemplate.class);
    private static final String appkey = "LAJFOWFALSKDJFALLKAJSDFLKSDFJLWKJ";

    private static final String host = "115.29.96.85";
    private static final int    port = 2181;
    private static final String appName = "crm";

    public static void main(String[] args) {
        Messagebus client = Messagebus.getInstance(appkey);
        client.setZkHost(host);
        client.setZkPort(port);

        IConsumer consumer = null;
        try {
            client.open();
            consumer = client.getConsumer();
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        } catch (MessagebusUnOpenException e) {
            e.printStackTrace();
        }

        //consume
        List<Message> msgs = consumer.consume(appName, 2);
        client.close();

        for (Message msg : msgs) {
            logger.info("message id : " + msg.getMessageHeader().getMessageId());
        }

    }

}
