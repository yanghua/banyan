package com.freedom.messagebus.httpbridge.util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;

public class ResponseUtil {

    public static void response(HttpServletResponse response,
                                int statusCode,
                                String err,
                                String msg,
                                String data) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json");
        String responseStr = "{ \"statusCode\" : " + statusCode + "," +
            " \"error\" : \"" + err + "\"," +
            " \"msg\" : \"" + msg + "\", " +
            " \"data\" : " + data +
            "}";
        response.setContentLength(responseStr.getBytes(Charset.defaultCharset()).length);
        PrintWriter writer = response.getWriter();
        writer.write(responseStr);
        writer.flush();
        writer.close();
    }

}
