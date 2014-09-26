package com.freedom.messagebus.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ShellHelper {

    private static final Log logger = LogFactory.getLog(ShellHelper.class);

    public static void exec(@NotNull String cmdStr) throws IOException, InterruptedException {
        String[] splitedParts = new String[]{
            "/bin/sh", "-c", cmdStr
        };
        Process process = Runtime.getRuntime().exec(splitedParts);
        process.waitFor();

        String errStr = translateFromStream(process.getErrorStream());
        logger.error("[exec] occurs a error, " + " and error msg is :" + errStr + " command is : " + cmdStr);
        logger.info("[exec] output info is " + translateFromStream(process.getInputStream()));
    }

    private static String translateFromStream(InputStream stream) throws IOException {
        BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);

        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, bytesRead));
        }

        return sb.toString();
    }

}
