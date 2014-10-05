package com.freedom.messagebus.httpbridge.controller;

import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.httpbridge.util.Consts;
import com.freedom.messagebus.httpbridge.util.ResponseUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class HttpBridge extends HttpServlet {

    private static final Log logger = LogFactory.getLog(HttpBridge.class);

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        logger.info("[service] url is : " + req.getRequestURI());
        String apiType = req.getParameter("type");

        if (apiType == null || apiType.isEmpty()) {
            logger.error("the query string : type can not be null or empty");
            ResponseUtil.response(resp, Consts.HTTP_FAILED_CODE,
                                  "the query string : type can not be null or empty",
                                  "","");
        } else {
            MessageCarryType msgCarryType = MessageCarryType.lookup(apiType);

            switch (msgCarryType) {
                case PRODUCE:
                    this.produce(req, resp);
                    break;

                case CONSUME:
                    this.consume(req, resp);
                    break;

                case REQUEST:
                    this.request(req, resp);
                    break;

                case RESPONSE:
                    this.response(req, resp);
                    break;

                default:
                    ResponseUtil.response(resp, Consts.HTTP_FAILED_CODE,
                                          "invalidated type", "", "");
            }
        }
    }

    private void produce(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        
    }

    private void consume(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    }

    private void request(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    }

    private void response(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    }
}
