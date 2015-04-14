package com.messagebus.server.daemon;

import com.messagebus.client.IRequestListener;
import com.messagebus.client.MessageResponseTimeoutException;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.common.HttpHelper;
import com.messagebus.server.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by yanghua on 3/23/15.
 */
public class SystemMonitorService extends BaseTestCase {

    private static final Log logger = LogFactory.getLog(SystemMonitorService.class);

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testServerRequestAndResponse() throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String secret = "iqwjasdfklakqoiajsidfoasidjoqw";
                MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
                final Messagebus client = singlePool.getResource();

                client.response(secret, new IRequestListener() {
                    @Override
                    public Message onRequest(Message requestMsg) {
                        String rabbitmqServerInfo = getRabbitmqServerInfo();
                        Message respMsg = MessageFactory.createMessage(MessageType.QueueMessage);
                        respMsg.setContent(rabbitmqServerInfo.getBytes());

                        return respMsg;
                    }
                }, 10, TimeUnit.SECONDS);


                singlePool.returnResource(client);
                singlePool.destroy();
            }

        }).start();

        String secret = "miuhqihusahdfuhaksjhfuiqweka";
        String token = "masdjfqiowieqooeirfajhfihfweld";
        MessagebusSinglePool singlePool = new MessagebusSinglePool(host, port);
        Messagebus client = singlePool.getResource();

        Message msg = MessageFactory.createMessage(MessageType.QueueMessage);
//        msg.setContentType("text/plain");
//        msg.setContentEncoding("utf-8");
//
//        msg.setContent("test".getBytes(com.messagebus.common.Constants.CHARSET_OF_UTF8));

        Message responseMsg = null;

        try {
            responseMsg = client.request(secret, "serverInfoResponse", msg, token, 10);
        } catch (MessageResponseTimeoutException e) {
            e.printStackTrace();
        }

        singlePool.returnResource(client);
        singlePool.destroy();

        assertNotNull(responseMsg);
//        assertEquals("test", new String(responseMsg.getContent(), com.messagebus.common.Constants.CHARSET_OF_UTF8));

    }

    private String getRabbitmqServerInfo() {
        Map<String, Object> requestParamDic = new HashMap<String, Object>(3);
        requestParamDic.put("host", Constants.HOST);
        requestParamDic.put("port", Constants.PORT);
        requestParamDic.put("path", Constants.HTTP_API_OVERVIEW);
        return HttpHelper.syncHTTPGet(requestParamDic, Constants.DEFAULT_AUTH_INFO);
    }
}
