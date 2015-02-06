package com.freedom.messagebus.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * the util class for http request
 */
public class HttpHelper {

    private static final Log logger = LogFactory.getLog(HttpHelper.class);


    public static String syncHTTPGet(Map<String, Object> requestParamDic, AuthInfo authInfo) {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        CloseableHttpResponse response = null;
        try {
            URI uri = new URIBuilder()
                .setScheme("http")
                .setHost(requestParamDic.get("host").toString())
                .setPort(Integer.parseInt(requestParamDic.get("port").toString()))
                .setPath(requestParamDic.get("path").toString())
                .setUserInfo(authInfo.getUserName(), authInfo.getPassword())
                .build();
            HttpGet httpGet = new HttpGet(uri);
            response = httpClient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                long len = entity.getContentLength();
                if (len == -1)
                    return "";

                if (len < 2 * 1024 * 1024) {
                    return EntityUtils.toString(entity);
                } else {
                    logger.error("[syncHTTPGet] response length is too large : (" + len + ") B " +
                                     "; and the url is : " + uri.getRawPath());
                    return "";
                }
            }
        } catch (IOException | URISyntaxException e) {
            logger.error("[syncHTTPGet] occurs a IOException : " + e.getMessage());
        } finally {
            if (response != null)
                try {
                    response.close();
                } catch (IOException e) {
                    logger.error("[syncHTTPGet] finally block occurs a IOException : " + e.getMessage());
                }
        }

        return "";
    }

    public static void asyncHTTPGet(String urlStr, AuthInfo authInfo, IAsyncCallback callback) {

    }

    public static void syncHTTPPut(String urlStr, AuthInfo authInfo) {

    }

    public static void asyncHTTPPut(String urlStr, AuthInfo authInfo, IAsyncCallback callback) {

    }

    public static void syncHTTPPost(String urlStr, AuthInfo authInfo) {

    }

    public static void asyncHTTPPost(String urlStr, AuthInfo authInfo, IAsyncCallback callback) {

    }

    public static void syncHTTPDelete(String urlStr, AuthInfo authInfo) {

    }

    public static void asyncHTTPDelete(String urlStr, AuthInfo authInfo, IAsyncCallback callback) {

    }

}
