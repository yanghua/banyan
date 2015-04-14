package com.messagebus.server.daemon;

import com.messagebus.client.IRequestListener;
import com.messagebus.client.MessageResponseTimeoutException;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 3/24/15.
 */
public class CommandService extends BaseTestCase {

    private static final Log logger = LogFactory.getLog(CommandService.class);

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCommandPingPong() throws Exception {
        //response
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String secret = "nadjfqulaudhfkauwaudhfakqajd";
                MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
                final Messagebus client = singlePool.getResource();

                client.response(secret, new IRequestListener() {
                    @Override
                    public Message onRequest(Message requestMsg) {

                        //check command is ping...
                        Map<String, Object> headers = requestMsg.getHeaders();
                        if (logger.isDebugEnabled()) {
                            logger.debug("is header not null : " + (headers != null));
                            logger.debug("is contain COMMAND key : " + (headers.containsKey("COMMAND")));
                            logger.debug("COMMAND value is : " + headers.get("COMMAND"));
                        }

                        boolean baseCheck = (headers != null && headers.containsKey("COMMAND"));

                        Message respMsg = MessageFactory.createMessage(MessageType.QueueMessage);

                        if (baseCheck) {
                            String cmd = headers.get("COMMAND").toString();
                            logger.debug("received " + cmd + " command from app id : ");
                            if (cmd.equals("PING")) {
                                respMsg.setContent("PONG".getBytes());
                            } else {
                                respMsg.setContent("ERROR".getBytes());
                            }
                        } else {
                            respMsg.setContent("ERROR".getBytes());
                        }

                        return respMsg;
                    }
                }, 10, TimeUnit.SECONDS);

                singlePool.returnResource(client);
                singlePool.destroy();
            }

        }).start();

        String secret = "lauhsdjkfhqiuwequhiausdfhuah";
        String token = "masuehiuiauhfiuqoquhaisudfhuqe";
        MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
        Messagebus client = singlePool.getResource();

        Message msg = MessageFactory.createMessage(MessageType.QueueMessage);
        msg.setContentType("text/plain");
        msg.setContentEncoding("utf-8");

        Map<String, Object> headerMap = new HashMap<String, Object>(1);
        headerMap.put("COMMAND", "PING");
        msg.setHeaders(headerMap);

        msg.setContent("".getBytes(com.messagebus.common.Constants.CHARSET_OF_UTF8));

        Message responseMsg = null;

        try {
            responseMsg = client.request(secret, "serverCmdResponse", msg, token, 10);
        } catch (MessageResponseTimeoutException e) {
            e.printStackTrace();
        }

        singlePool.returnResource(client);
        singlePool.destroy();

        assertNotNull(responseMsg);
        assertEquals("PONG", new String(responseMsg.getContent(), com.messagebus.common.Constants.CHARSET_OF_UTF8));

    }


}
