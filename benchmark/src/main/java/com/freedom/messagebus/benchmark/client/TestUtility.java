package com.freedom.messagebus.benchmark.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.annotations.NotNull;

import java.io.*;

public class TestUtility {

    private final static Log logger = LogFactory.getLog(TestUtility.class);

    public static void writeFile(@NotNull String fileName, @NotNull long[] xArr, @NotNull long[] yArr) {
        String filePath = String.format(TestConfigConstant.OUTPUT_FILE_PATH_FORMAT, fileName);

        PrintWriter out = null;

        try {
            logger.info(filePath);
            File dataFile = new File(filePath);
            if (!dataFile.exists()) {
                dataFile.createNewFile();
            }

            out = new PrintWriter(new FileWriter(filePath));
            out.println("#x y");

            for (int i = 0; i < xArr.length; i++) {
                out.println(xArr[i] + " " + yArr[i]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != out) {
                out.close();
            }
        }

    }

    public static void exec(String[] cmds, boolean hasOutput) {
        if (cmds == null || cmds.length == 0) {
            return;
        }

        try {
            Process process = Runtime.getRuntime().exec(cmds);
            if (hasOutput) {
                InputStreamReader ir = new InputStreamReader(process.getInputStream());
                LineNumberReader input = new LineNumberReader(ir);
                String line;
                while ((line = input.readLine()) != null)
                    System.out.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
