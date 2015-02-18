package com.freedom.messagebus.client;

import com.freedom.messagebus.client.message.model.Message;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * a sync consumer.
 */
public class SyncConsumer  {

    private static final Log logger = LogFactory.getLog(SyncConsumer.class);

    private IConsumer consumer;

    public SyncConsumer(IConsumer consumer) {
        this.consumer = consumer;
    }

    public List<Message> consume(String appName, int expectedNum) {
        return this.consumer.consume(appName, expectedNum);
    }
}
