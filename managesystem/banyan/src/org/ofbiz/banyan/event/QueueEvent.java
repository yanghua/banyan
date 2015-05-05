package org.ofbiz.banyan.event;

import org.ofbiz.service.ModelService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by yanghua on 3/27/15.
 */
public class QueueEvent {

    public static final String module = QueueEvent.class.getName();

    public static String createQueue(HttpServletRequest request, HttpServletResponse response) {
        return ModelService.RESPOND_SUCCESS;
    }

    public static String updateQueue(HttpServletRequest request, HttpServletResponse response) {
        return "success";
    }

    public static String auditQueue(HttpServletRequest request, HttpServletResponse response) {
        return ModelService.RESPOND_SUCCESS;
    }

}
