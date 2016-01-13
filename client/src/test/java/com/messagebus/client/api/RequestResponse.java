package com.messagebus.client.api;

import com.google.common.base.Strings;
import com.messagebus.client.IRequestListener;
import com.messagebus.client.MessageResponseTimeoutException;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusSinglePool;
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
public class RequestResponse extends BaseTestCase {

    private static final Log logger = LogFactory.getLog(RequestResponse.class);

    @Override
    public void setUp() throws Exception {

    }

    @Override
    public void tearDown() throws Exception {

    }

    public void testRequestAndResponse() throws Exception {
        new Thread(new Runnable() {

            @Override
            public void run() {
                final String         secret     = "muciasnajjkasbdfbaskjdfkjkasja";
                MessagebusSinglePool singlePool = new MessagebusSinglePool(zkConnectionStr);
                final Messagebus     client     = singlePool.getResource();

                client.response(secret, new IRequestListener() {

                    @Override
                    public Message onRequest(Message requestMsg) {
                        if (Strings.isNullOrEmpty(requestMsg.getCorrelationId())) {
                            logger.info("got requested message : " + requestMsg.getCorrelationId());
                        }

                        assertNotNull(requestMsg);
                        assertEquals("test", new String(requestMsg.getContent(), Constants.CHARSET_OF_UTF8));

                        Message respMsg = MessageFactory.createMessage();
                        respMsg.setContentType("text/plain");
                        respMsg.setContentEncoding("utf-8");
                        respMsg.setCorrelationId(requestMsg.getCorrelationId());

                        respMsg.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));

                        return respMsg;
                    }

                }, 15, TimeUnit.SECONDS);

                singlePool.returnResource(client);
                singlePool.destroy();
            }

        }).start();

        String               secret     = "iuoqiwejicaoisfaisfbsqewnfjnfa";
        String               token      = "cakjdhfjasdflqjoiajsdjflqkuwef";
        MessagebusSinglePool singlePool = new MessagebusSinglePool(zkConnectionStr);
        Messagebus           client     = singlePool.getResource();

        Message msg = MessageFactory.createMessage();
        msg.setContentType("text/plain");
        msg.setContentEncoding("utf-8");

        msg.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));

        Message responseMsg = null;

        try {
            responseMsg = client.request(secret, "erpDemoResponse", msg, token, 10);
        } catch (MessageResponseTimeoutException e) {
            e.printStackTrace();
        }

        singlePool.returnResource(client);
        singlePool.destroy();

        assertNotNull(responseMsg);
        assertEquals("test", new String(responseMsg.getContent(), Constants.CHARSET_OF_UTF8));
    }
}
