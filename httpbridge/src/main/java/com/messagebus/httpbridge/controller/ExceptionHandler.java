package com.messagebus.httpbridge.controller;

import com.messagebus.httpbridge.util.Constants;
import com.messagebus.httpbridge.util.ResponseUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by yanghua on 3/31/15.
 */
public class ExceptionHandler extends HttpServlet {

    private static final Log logger = LogFactory.getLog(ExceptionHandler.class);

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = (String) req.getAttribute("javax.servlet.error.request_uri");
        Object exception = req.getAttribute("javax.servlet.error.exception");

        logger.error("error uri : " + uri);
        logger.error("error type : " + exception);

        ResponseUtil.response(resp, Constants.HTTP_FAILED_CODE, exception.toString(), "", "\"\"");
    }
}
