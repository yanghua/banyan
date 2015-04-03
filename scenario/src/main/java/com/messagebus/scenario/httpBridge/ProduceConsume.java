package com.messagebus.scenario.httpBridge;

import com.messagebus.client.message.model.*;
import com.messagebus.common.Constants;
import com.messagebus.scenario.util.PropertiesHelper;
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

    public static void main(String[] args) {
//        testProduceWithPost();

        testProduceWithGet();

        testConsume("pull");
    }

    private static void testProduceWithPost() {
        String testUrlFormat = "http://%s:%s/messagebus/queues/%s/messages?secret=%s&apiType=produce&token=%s";
        String testQueue = "appDataQueue";
        String secret = "iojawdnaisdflknoiankjfdblaidcas";
        String token = "iojawdnaisdflknoiankjfdblaidcas";

        String url = String.format(testUrlFormat,
                                   PropertiesHelper.getPropertyValue("messagebus.httpbridge.host"),
                                   Integer.parseInt(PropertiesHelper.getPropertyValue("messagebus.httpbridge.port")),
                                   testQueue,
                                   secret,
                                   token);

        CloseableHttpClient httpClient = HttpClients.createDefault();

        CloseableHttpResponse response = null;

        Message msg = MessageFactory.createMessage(MessageType.QueueMessage);
        msg.setReplyTo(testQueue);
        msg.setContentType("text/plain");

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

    private static void testProduceWithGet() {
        String testUrlFormat = "http://%s:%s/messagebus/queues/%s/messages?secret=%s&apiType=produce&token=%s";
        String testQueue = "appDataQueue";
        String secret = "iojawdnaisdflknoiankjfdblaidcas";
        String token = "iojawdnaisdflknoiankjfdblaidcas";

        String url = String.format(testUrlFormat,
                                   PropertiesHelper.getPropertyValue("messagebus.httpbridge.host"),
                                   Integer.parseInt(PropertiesHelper.getPropertyValue("messagebus.httpbridge.port")),
                                   testQueue,
                                   secret,
                                   token);
        url += "&content=text&contentType=text/plain";

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

    private static void testConsume(String mode) {
        String testUrlFormat = "http://%s:%s/messagebus/queues/messages?secret=%s&apiType=consume&mode=%s&num=1";
        String secret = "iojawdnaisdflknoiankjfdblaidcas";

        String url = String.format(testUrlFormat,
                                   PropertiesHelper.getPropertyValue("messagebus.httpbridge.host"),
                                   Integer.parseInt(PropertiesHelper.getPropertyValue("messagebus.httpbridge.port")),
                                   secret,
                                   mode);

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
