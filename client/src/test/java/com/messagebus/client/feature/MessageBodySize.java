package com.messagebus.client.feature;

import com.messagebus.client.core.BaseTestCase;
import com.messagebus.client.core.MessageUtil;
import com.messagebus.client.message.model.Message;

/**
 * Created by yanghua on 3/27/15.
 */
public class MessageBodySize extends BaseTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMsgBodySizeOverhead() throws Exception {
        String secret = "kljasdoifqoikjhhhqwhebasdfasdf";
        String token  = "hlkasjdhfkqlwhlfalksjdhgssssas";

        Message msg = MessageUtil.create(4000);

        client.produce(secret, "emapDemoConsume", msg, token);
    }
}


