package com.freedom.messagebus.benchmark.client;

import com.freedom.messagebus.client.Messagebus;
import com.freedom.messagebus.client.MessagebusConnectedFailedException;
import com.freedom.messagebus.client.MessagebusUnOpenException;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.model.MessageType;
import com.freedom.messagebus.common.ExceptionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

public class TestUtility {

    private final static Log logger = LogFactory.getLog(TestUtility.class);

    public static void writeFile(String fileName, long[] xArr, long[] yArr) {
        String filePath = String.format(TestConfigConstant.OUTPUT_FILE_PATH_FORMAT, fileName);

        File dataFile = new File(filePath);
        try {
            if (!dataFile.exists() && (!dataFile.createNewFile())) {
                throw new RuntimeException("create new file at : " + filePath + " , failure.");
            }
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "writeFile");
            throw new RuntimeException(e);
        }

        try (FileWriter fileWriter = new FileWriter(filePath);
             PrintWriter out = new PrintWriter(fileWriter)) {
            out.println("#x y");

            for (int i = 0; i < xArr.length; i++) {
                out.println(xArr[i] + " " + yArr[i]);
            }
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "writeFile");
            throw new RuntimeException(e);
        }
    }

    public static void exec(String[] cmds, boolean hasOutput) {
        if (cmds == null || cmds.length == 0) {
            return;
        }

        if (hasOutput) {
            Process process = null;
            try {
                process = Runtime.getRuntime().exec(cmds);
            } catch (IOException e) {
                ExceptionHelper.logException(logger, e, "exec");
                throw new RuntimeException(e.toString());
            }

            try (InputStreamReader ir = new InputStreamReader(process.getInputStream());
                 LineNumberReader input = new LineNumberReader(ir)) {
                String line;
                while ((line = input.readLine()) != null)
                    System.out.println(line);
            } catch (IOException e) {
                ExceptionHelper.logException(logger, e, "exec");
                throw new RuntimeException(e.toString());
            }
        }
    }

    public static void produce(long total) {
        Message msg = TestMessageFactory.create(MessageType.QueueMessage, TestConfigConstant.MSG_BODY_SIZE_OF_KB);

        Messagebus client = new Messagebus(TestConfigConstant.APP_KEY);
        client.setPubsuberHost(TestConfigConstant.HOST);
        client.setPubsuberPort(TestConfigConstant.PORT);
        try {
            client.open();

            for (int i = 0; i < total; i++) {
                client.produce(msg, TestConfigConstant.QUEUE_NAME);
            }
        } catch (MessagebusConnectedFailedException e) {
            e.printStackTrace();
        } catch (MessagebusUnOpenException e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }

}
