package com.messagebus.scenario.httpBridge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.messagebus.client.message.model.*;
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
import java.util.concurrent.TimeUnit;

public class ResponseTemplate {

    //there are two end points, follow there steps:
    /*
        send a request (end point 1)
        get a response (end point 2)
        send a response (end point 2)
        get a response (end point 1)
    */

    private static final Log logger = LogFactory.getLog(ResponseTemplate.class);

    private static String testUrlFormat = "http://%s:%s/messagebus/queues/%s/messages?appkey=%s&type=%s&timeout=%s";
    private static String testHost      = "localhost";
    private static int    testPort      = 8081;
    private static String testQueue     = "crm";
    private static String appkey        = "jahksjdfhakjdflkasdjflk";
    private static long   timeout       = 30000;

    private static volatile Object lockObj = new Object();

    public static void main(String[] args) {
        try {
            EndPoint1 e1 = new EndPoint1();
            e1.start();

            TimeUnit.SECONDS.sleep(3);

            EndPoint2 e2 = new EndPoint2();
            e2.start();

            //block 30s
            TimeUnit.SECONDS.sleep(40);
        } catch (InterruptedException e) {

        }
    }

    private static class EndPoint1 extends Thread {

        @Override
        public void run() {
            CloseableHttpResponse response = null;
            try {
                String url = String.format(testUrlFormat, testHost, testPort, testQueue, appkey, "request", timeout);

                CloseableHttpClient httpClient = HttpClients.createDefault();

                Message testMsg = MessageFactory.createMessage(MessageType.QueueMessage);
                QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
                body.setContent("test".getBytes());
                testMsg.setMessageBody(body);

                String msg2json = MessageJSONSerializer.serialize(testMsg);

                HttpPost postRequest = new HttpPost(url);
                List<NameValuePair> nvps = new ArrayList<>();
                nvps.add(new BasicNameValuePair("message", msg2json));
                postRequest.setEntity(new UrlEncodedFormEntity(nvps));

                response = httpClient.execute(postRequest);

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    logger.info("end point 1 received response : " + EntityUtils.toString(entity));
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

    private static class EndPoint2 extends Thread {

        @Override
        public void run() {
            CloseableHttpResponse response = null;
            CloseableHttpResponse resp = null;
            try {
                synchronized (lockObj) {
                    String url = String.format(testUrlFormat, testHost, testPort, testQueue, appkey, "consume", timeout);
                    url += "&mode=sync&num=1";

                    CloseableHttpClient httpClient = HttpClients.createDefault();
                    HttpGet get = new HttpGet(url);

                    //get request
                    response = httpClient.execute(get);
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        String responseData = EntityUtils.toString(entity);
                        logger.info("end point 2 : received response : " + responseData);

                        Message msg = extractRequestMsg(responseData);

                        String tmpQueueName = String.valueOf(msg.getMessageHeader().getMessageId());

                        String responseUrl = String.format(testUrlFormat, testHost, testPort,
                                                           tmpQueueName, appkey, "response", timeout);

                        //send response
                        CloseableHttpClient responseHttpClient = HttpClients.createDefault();


                        Message testMsg = MessageFactory.createMessage(MessageType.QueueMessage);
                        QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
                        body.setContent("test".getBytes());
                        testMsg.setMessageBody(body);

                        String msg2json = MessageJSONSerializer.serialize(testMsg);

                        HttpPost postRequest = new HttpPost(responseUrl);
                        List<NameValuePair> nvps = new ArrayList<>();
                        nvps.add(new BasicNameValuePair("message", msg2json));
                        postRequest.setEntity(new UrlEncodedFormEntity(nvps));

                        resp = responseHttpClient.execute(postRequest);
                        HttpEntity responseEntity = response.getEntity();

                        lockObj.notify();
                    }
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

                if (resp != null)
                    try {
                        resp.close();
                    } catch (IOException e) {
                        logger.error("[syncHTTPGet] finally block occurs a IOException : " + e.getMessage());
                    }
            }
        }
    }

    private static Message extractRequestMsg(String respData) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(respData);
        JsonObject object = element.getAsJsonObject();
        JsonElement dataElement = object.get("data");
        if (!dataElement.isJsonArray()) {
            return null;
        }

        JsonElement msgElement = dataElement.getAsJsonArray().get(0);

        return MessageJSONSerializer.deSerialize(msgElement, MessageType.QueueMessage);
    }


}
