package com.freedom.messagebus.httpbridge.util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ResponseUtil {

    public static void response(HttpServletResponse response,
                                int statusCode,
                                String err,
                                String msg,
                                String data) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.setContentType("application/json");
        PrintWriter writer = response.getWriter();
        writer.write("{ \"statusCode\" : " + statusCode + "," +
                         " \"error\" : \"" + err + "\"," +
                         " \"msg\" : \"" + msg + "\", " +
                         " \"data\" : " + data +
                         "}");
        writer.flush();
        writer.close();
    }

}
