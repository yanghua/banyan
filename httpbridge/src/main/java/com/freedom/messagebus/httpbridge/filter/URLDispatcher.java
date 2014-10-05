package com.freedom.messagebus.httpbridge.filter;

import com.freedom.messagebus.httpbridge.util.Consts;
import com.freedom.messagebus.httpbridge.util.ResponseUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.jvm.hotspot.debugger.posix.elf.ELFSectionHeader;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by yanghua on 9/30/14.
 */
public class URLDispatcher implements Filter {

    private static final Log logger = LogFactory.getLog(URLDispatcher.class);
    private static final String URI_PREFIX = "/messagebus/queues";
    private static final String THE_KEY_OF_APP_KEY = "appkey";


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String uri = request.getRequestURI();
        if (!uri.startsWith(URI_PREFIX)) {
            logger.error("[doFilter] request uri is " + request.getRequestURI());
            ResponseUtil.response((HttpServletResponse)servletResponse, Consts.HTTP_NOT_FOUND_CODE,
                                  "the request uri : " + request.getRequestURI() + "is not found!",
                                  "", "");
        } else {
            String appKeyVal = request.getParameter(THE_KEY_OF_APP_KEY);
            if (appKeyVal == null || appKeyVal.isEmpty()) {
                logger.error("[doFilter] missed query string : appkey");
                ResponseUtil.response((HttpServletResponse)servletResponse, Consts.HTTP_FAILED_CODE,
                                      "missed query string : appkey!", "", "");
            } else {
                filterChain.doFilter(request, servletResponse);
            }
        }
    }

    @Override
    public void destroy() {

    }
}
