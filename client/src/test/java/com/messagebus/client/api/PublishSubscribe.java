package com.messagebus.client.api;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.core.BaseTestCase;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 3/20/15.
 */
public class PublishSubscribe extends BaseTestCase {

    private static final Log logger = LogFactory.getLog(PublishSubscribe.class);

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testPublishAndSubscribe() throws Exception {
        String secret = "oiqwenncuicnsdfuasdfnkajkwqowe";

        Message msg = MessageFactory.createMessage();
        msg.setContentType("text/plain");
        msg.setContentEncoding("utf-8");

        msg.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));

        client.publish(secret, new Message[]{msg});

        //--------------------------subscribe-------------------------------

        secret = "nckljsenlkjanefluiwnlanfmsdfas";
        String token = "nclajsdljhqiuwehfiusaiudfhiausd";
        client.subscribe(secret, "erpDemoPublish", token, new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {
                assertNotNull(message);
                assertEquals("test", new String(message.getContent(), Constants.CHARSET_OF_UTF8));
            }
        }, 3, TimeUnit.SECONDS);

    }
}
