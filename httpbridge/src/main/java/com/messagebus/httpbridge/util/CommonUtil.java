package com.messagebus.httpbridge.util;

import com.google.common.base.Strings;

/**
 * Created by yanghua on 3/30/15.
 */
public class CommonUtil {

    public static boolean validMessageType(String msgTypeStr) {
        if (Strings.isNullOrEmpty(msgTypeStr)) {
            return false;
        }

        return msgTypeStr.equals(Constants.TEXT_PLAIN_CONTENT_TYPE);
    }

}
