package com.messagebus.client.feature;

import com.messagebus.client.core.BaseTestCase;
import com.messagebus.client.core.MessageUtil;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageType;

/**
 * Created by yanghua on 3/27/15.
 */
public class MessageNumLimit extends BaseTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMessageNumLimit() throws Exception {
        String secret = "muqwejlaksdfkljaliqwejflkasdfs";
        String token = "jhlkasdfkjhasdfqwkasdfjqkwjhas";

        Message msg = MessageUtil.create(MessageType.QueueMessage, 500);

        for (int i = 0; i < 300; i++) {
            client.produce(secret, "erpDemoProduce-Consume", msg, token);
        }

    }
}
