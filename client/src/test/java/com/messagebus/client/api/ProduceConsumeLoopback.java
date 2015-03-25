package com.messagebus.client.api;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.common.Constants;

import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 3/25/15.
 */
public class ProduceConsumeLoopback extends BaseTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private void commonProduce() {
        String secret = "jnmzqwemnjaksdfqjnkajfjasndfnw";
        String token = "jnmzqwemnjaksdfqjnkajfjasndfnw";

        Message msg = MessageFactory.createMessage(MessageType.QueueMessage);
        msg.setContentType("text/plain");
        msg.setContentEncoding("utf-8");
        msg.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));

        client.produce(secret, "erpDemoProduce-Consume", msg, token);
    }

    public void testLoopBack() throws Exception {
        commonProduce();

        String consumeSecret = "jnmzqwemnjaksdfqjnkajfjasndfnw";
        client.consume(consumeSecret, 2, TimeUnit.SECONDS, new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {
                assertNotNull(message);
                assertEquals("test", new String(message.getContent(), Constants.CHARSET_OF_UTF8));
            }
        });
    }
}
