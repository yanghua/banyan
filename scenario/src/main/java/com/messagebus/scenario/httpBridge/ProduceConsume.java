package com.messagebus.scenario.httpBridge;

import com.google.gson.Gson;
import com.messagebus.client.message.model.*;
import com.messagebus.common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProduceConsume {

    private static final Log logger = LogFactory.getLog(ProduceConsume.class);

    private static final Gson gson = new Gson();

    private static String testHost = "172.16.206.29";
    private static int    testPort = 8080;

    public static void main(String[] args) {
        testProduce();

        testConsume("push");
    }

    private static void testProduce() {
        String testUrlFormat = "http://%s:%s/messagebus/queues/%s/messages?secret=%s&type=produce&token=%s";
        String testQueue = "emapDemoConsume";
        String secret = "kljasdoifqoikjhhhqwhebasdfasdf";
        String token = "hlkasjdhfkqlwhlfalksjdhgssssas";

        String url = String.format(testUrlFormat, testHost, testPort, testQueue, secret, token);

        CloseableHttpClient httpClient = HttpClients.createDefault();

        CloseableHttpResponse response = null;

        Message msg = MessageFactory.createMessage(MessageType.QueueMessage);
        msg.setReplyTo(testQueue);

        msg.setContent("test".getBytes(Constants.CHARSET_OF_UTF8));

        List<Message> msgs = new ArrayList<>(1);
        msgs.add(msg);
        String msgs2json = MessageJSONSerializer.serializeMessages(msgs);

        try {
            HttpPost postRequest = new HttpPost(url);
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("messages", msgs2json));
            postRequest.setEntity(new UrlEncodedFormEntity(nvps));

            response = httpClient.execute(postRequest);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                logger.info("response is : " + EntityUtils.toString(entity));
            }
        } catch (IOException e) {
            logger.error("[syncHTTPGet] occurs a IOException : " + e.getMessage());
        } finally {
            if (response != null)
                try {
                    response.close();
                } catch (IOException e) {
                    logger.error("[syncHTTPGet] finally block occurs a IOException : " + e.getMessage());
                }
        }
    }

    private static void testConsume(String mode) {
        String testUrlFormat = "http://%s:%s/messagebus/queues/messages?secret=%s&type=consume&mode=%s&num=1";
        String secret = "zxdjnflakwenklasjdflkqpiasdfnj";

        String url = String.format(testUrlFormat, testHost, testPort, secret, mode);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;

        try {
            response = httpClient.execute(new HttpGet(url));

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                logger.info("response is : " + EntityUtils.toString(entity));
            }
        } catch (IOException e) {
            logger.error("[syncHTTPGet] occurs a IOException : " + e.getMessage());
        } finally {
            if (response != null)
                try {
                    response.close();
                } catch (IOException e) {
                    logger.error("[syncHTTPGet] finally block occurs a IOException : " + e.getMessage());
                }
        }
    }

}
