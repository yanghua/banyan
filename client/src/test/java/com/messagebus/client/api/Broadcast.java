package com.messagebus.client.api;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 3/20/15.
 */
public class Broadcast extends BaseTestCase {

    private static final Log logger = LogFactory.getLog(Broadcast.class);

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testBroadcast() throws Exception {
        String secret = "mucasdjfaskdufhqiiuuasdfasdnus";
        String token = "qiakdjfanekisdfadfhkqljwqheu";

        Message msg = MessageFactory.createMessage(MessageType.BroadcastMessage);
        msg.setContentType("text/plain");
        msg.setContentEncoding("utf-8");

        msg.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));

        client.broadcast(secret, new Message[]{msg}, token);

        //-------------------------------------------------------

        secret = "kjhasdfhlkuqjhgaebjhasgdfabfak";
        //notification handler
        client.setNotificationListener(new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {
                assertNotNull(message);
                assertEquals("test", new String(message.getContent(), Constants.CHARSET_OF_UTF8));
            }
        });

        //business handler
        client.consume(secret, 3, TimeUnit.SECONDS, new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {

            }
        });

        secret = "zxdjnflakwenklasjdflkqpiasdfnj";
        client.setNotificationListener(new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {
                assertNotNull(message);
                assertEquals("test", new String(message.getContent(), Constants.CHARSET_OF_UTF8));
            }
        });

        client.consume(secret, 3, TimeUnit.SECONDS, new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {

            }
        });

    }
}
