package com.freedom.messagebus.common;

import org.apache.commons.logging.Log;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHelper {

    public static String extractStackTrace(Throwable t) {
        if (t == null) {
            return "";
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        try {
            t.printStackTrace(pw);
            return sw.toString();
        } finally {
            pw.close();
        }
    }

    public static void logException(final Log logger, Throwable t, String additionalInfo) {
        if (additionalInfo != null)
            logger.error(additionalInfo);
        logger.error("-*- stacktrace -*- : " + extractStackTrace(t));
        logger.error("-*- course -*- : " + extractStackTrace(t.getCause()));
    }

}
