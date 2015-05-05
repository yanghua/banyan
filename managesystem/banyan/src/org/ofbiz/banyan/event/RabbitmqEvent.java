package org.ofbiz.banyan.event;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by yanghua on 3/23/15.
 */
public class RabbitmqEvent {

    public static final String module = RabbitmqEvent.class.getName();

    public static String requestRabbitmqInfo(HttpServletRequest request, HttpServletResponse response) {
        GenericValue userLogin = (GenericValue) request.getSession().getAttribute("userLogin");
        LocalDispatcher dispatcher = (LocalDispatcher) request.getAttribute("dispatcher");
        Map<String, ? extends Object> ctx = UtilMisc.toMap("userLogin", userLogin);
        String jsonStr;
        try {
            Map<String, Object> result = dispatcher.runSync("requestRabbitmqInfoService", ctx);
            jsonStr = result.get("result").toString();

            response.setContentType("application/x-json");
            response.setContentLength(jsonStr.getBytes(Charset.defaultCharset()).length);

            Writer out = null;
            try {
                out = response.getWriter();
                out.write(jsonStr);
                out.flush();
            } catch (IOException e) {
                Debug.logError(e, module);
                if (null != out) {
                    try {
                        out.close();
                    } catch (IOException e1) {
                        Debug.logError(e1, module);
                    }
                }
            }

            return ModelService.RESPOND_SUCCESS;
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            return ModelService.RESPOND_ERROR;
        }
    }
}
