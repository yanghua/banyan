package com.messagebus.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ShellHelper {

    private static final Log logger = LogFactory.getLog(ShellHelper.class);

    public static ExecResult exec(String cmdStr) throws IOException, InterruptedException {
        String[] cmd = {"/bin/sh", "-c", cmdStr};
        Process process = Runtime.getRuntime().exec(cmd);
        process.waitFor();

        String errStr = translateFromStream(process.getErrorStream());
        String infoStr = translateFromStream(process.getInputStream());

        ExecResult execResult = new ExecResult();
        execResult.setError(errStr);
        execResult.setInfo(infoStr);

        logger.debug("[exec] occurs a error, " + " and error msg is :" + errStr + " command is : " + cmdStr);
        logger.debug("[exec] output info is " + infoStr);

        return execResult;
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

    public static class ExecResult {

        private String info;
        private String error;

        public ExecResult() {
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        @Override
        public String toString() {
            return "ExecResult{" +
                "info='" + info + '\'' +
                ", error='" + error + '\'' +
                '}';
        }
    }
}
