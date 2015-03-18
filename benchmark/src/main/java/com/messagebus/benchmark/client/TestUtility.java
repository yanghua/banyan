package com.messagebus.benchmark.client;

import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.MessagebusUnOpenException;
import com.messagebus.client.message.model.IMessage;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.common.ExceptionHelper;
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
        IMessage msg = TestMessageFactory.create(MessageType.QueueMessage, TestConfigConstant.MSG_BODY_SIZE_OF_KB);

        MessagebusSinglePool singlePool = new MessagebusSinglePool(TestConfigConstant.HOST,
                                                                   TestConfigConstant.PORT);
        Messagebus client = singlePool.getResource();

        try {
            for (int i = 0; i < total; i++) {
//                client.produce(, TestConfigConstant.QUEUE_NAME, msg, );
            }
        } catch (MessagebusUnOpenException e) {
            e.printStackTrace();
        } finally {
            singlePool.returnResource(client);
            singlePool.destroy();
        }
    }

}
