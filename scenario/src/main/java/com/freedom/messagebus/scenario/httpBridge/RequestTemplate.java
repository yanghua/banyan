package com.freedom.messagebus.scenario.httpBridge;

import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.model.MessageFactory;
import com.freedom.messagebus.client.message.model.MessageType;
import com.freedom.messagebus.client.message.model.QueueMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class RequestTemplate {

    private static final Log logger = LogFactory.getLog(RequestTemplate.class);

    private static String testUrlFormat = "http://%s:%s/messagebus/queues/%s/messages?appkey=%s&type=request&timeout=%s";
    private static String testHost      = "localhost";
    private static int    testPort      = 8081;
    private static String testQueue     = "crm";
    private static String appkey        = "jahksjdfhakjdflkasdjflk";
    private static long   timeout       = 5000;

    public static void main(String[] args) {
        String url = String.format(testUrlFormat, testHost, testPort, testQueue, appkey, timeout);

        CloseableHttpClient httpClient = HttpClients.createDefault();

        CloseableHttpResponse response = null;

        Message msg = MessageFactory.createMessage(MessageType.QueueMessage);
        msg.getMessageHeader().setReplyTo(testQueue);

        QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
        body.setContent("test".getBytes());
        msg.setMessageBody(body);

//        String msg2json = MessageJSONSerializer.serialize(testMsg);
//
//        try {
//            HttpPost postRequest = new HttpPost(url);
//            List<NameValuePair> nvps = new ArrayList<>();
//            nvps.add(new BasicNameValuePair("message", msg2json));
//            postRequest.setEntity(new UrlEncodedFormEntity(nvps));
//
//            response = httpClient.execute(postRequest);
//            HttpEntity entity = response.getEntity();
//            if (entity != null) {
//                long len = entity.getContentLength();
//                if (len == -1)
//                    logger.error("there is no response data.");
//                else if (len < 2 * 1024 * 1024) {
//                    logger.info("response is : " + EntityUtils.toString(entity));
//                } else {
//                    logger.error("[syncHTTPGet] response length is too large : (" + len + ") B " +
//                                     "; and the url is : " + url);
//                }
//            }
//        } catch (IOException e) {
//            logger.error("[syncHTTPGet] occurs a IOException : " + e.getMessage());
//        } finally {
//            if (response != null)
//                try {
//                    response.close();
//                } catch (IOException e) {
//                    logger.error("[syncHTTPGet] finally block occurs a IOException : " + e.getMessage());
//                }
//        }
    }

}
