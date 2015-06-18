package com.messagebus.client.api;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.core.BaseTestCase;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 3/20/15.
 */
public class ProduceConsume extends BaseTestCase {

    private static final Log logger = LogFactory.getLog(ProduceConsume.class);

    private String consumeSecret = "zxdjnflakwenklasjdflkqpiasdfnj";

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    private void commonProduce() {
        String secret = "kljasdoifqoikjhhhqwhebasdfasdf";
        String token = "hlkasjdhfkqlwhlfalksjdhgssssas";

        Message msg = MessageFactory.createMessage(MessageType.QueueMessage);
        msg.setContentType("text/plain");
        msg.setContentEncoding("utf-8");
        msg.setContent("test".getBytes());

        client.produce(secret, "emapDemoConsume", msg, token);
    }

    public void testSimpleProduceConsume() throws Exception {
        commonProduce();

        consumeSecret = "zxdjnflakwenklasjdflkqpiasdfnj";

        List<Message> msgs = client.consume(consumeSecret, 1);
        assertNotNull(msgs);
        assertEquals(1, msgs.size());
        Message result = msgs.get(0);
        assertNotNull(result);
        assertNotNull(result.getContent());
        assertEquals("test", new String(result.getContent()));

        for (Message item : msgs) {
            logger.info(item.getMessageId());
        }
    }

    public void testProduceAndConsumeWithPushStyle() {
        commonProduce();

        client.consume(consumeSecret, 10, TimeUnit.SECONDS, new IMessageReceiveListener() {
            @Override
            public void onMessage(Message message) {
                assertNotNull(message);
                assertEquals("test", new String(message.getContent(), Constants.CHARSET_OF_UTF8));
            }
        });
    }

}
