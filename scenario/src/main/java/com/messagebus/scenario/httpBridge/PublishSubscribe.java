package com.messagebus.scenario.httpBridge;

import com.google.gson.Gson;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageJSONSerializer;
import com.messagebus.client.message.model.MessageType;
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

/**
 * Created by yanghua on 3/19/15.
 */
public class PublishSubscribe {

    private static final Log logger = LogFactory.getLog(PublishSubscribe.class);

    private static final Gson gson = new Gson();

    private static String testHost = "localhost";
    private static int    testPort = 8080;

    public static void main(String[] args) {
        testPublish();

        testSubscribe();
    }

    private static void testPublish() {
        String testUrlFormat = "http://%s:%s/messagebus/queues/messages?secret=%s&type=publish&token=%s";
        String secret = "oiqwenncuicnsdfuasdfnkajkwqowe";
        String token = "kjkjasdjfhkajsdfhksdjhfkasdf";

        String url = String.format(testUrlFormat, testHost, testPort, secret, token);

        CloseableHttpClient httpClient = HttpClients.createDefault();

        CloseableHttpResponse response = null;

        Message msg = MessageFactory.createMessage(MessageType.QueueMessage);
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

    private static void testSubscribe() {
        String testUrlFormat = "http://%s:%s/messagebus/queues/messages?secret=%s&type=subscribe";
        String secret = "nckljsenlkjanefluiwnlanfmsdfas";

        String url = String.format(testUrlFormat, testHost, testPort, secret);

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
