package org.ofbiz.banyan.event;

import org.ofbiz.service.ModelService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by yanghua on 4/7/15.
 */
public class SinkEvent {

    public static final String module = SinkEvent.class.getName();

    public static String auditSink(HttpServletRequest request, HttpServletResponse response) {
        return ModelService.RESPOND_SUCCESS;
    }

    public static String switchSink(HttpServletRequest request, HttpServletResponse response) {
        return ModelService.RESPOND_SUCCESS;
    }
}
