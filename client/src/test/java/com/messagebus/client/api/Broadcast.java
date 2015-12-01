package com.messagebus.client.api;

import com.google.common.eventbus.Subscribe;
import com.messagebus.client.core.BaseTestCase;
import com.messagebus.client.event.component.NoticeEvent;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
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
        String secret = "kljasdoifqoikjhhhqwhebasdfasdf";

        Message msg = MessageFactory.createMessage();
        msg.setContentType("text/plain");
        msg.setContentEncoding("utf-8");

        msg.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));

        NotificationEventProcessor eventProcessor = new NotificationEventProcessor();
        client.registerEventProcessor(eventProcessor);

        client.broadcast(secret, new Message[]{msg});

        TimeUnit.SECONDS.sleep(5);

        client.unregisterEventProcessor(eventProcessor);
    }

    public static class NotificationEventProcessor {

        @Subscribe
        public void onNotification(NoticeEvent event) {
            logger.info("onNotification");
            Message message = event.getMsg();
            assertNotNull(message);
            assertEquals("test", new String(message.getContent(), Constants.CHARSET_OF_UTF8));
        }

    }
}
