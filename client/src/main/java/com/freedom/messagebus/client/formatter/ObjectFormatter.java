package com.freedom.messagebus.client.formatter;

import com.freedom.messagebus.client.core.message.Message;
import com.freedom.messagebus.client.core.message.ObjectMessage;
import com.freedom.messagebus.client.model.message.ObjectMessagePOJO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

/**
 * the object formatter
 */
public class ObjectFormatter implements IFormatter {

    private static final Log logger = LogFactory.getLog(ObjectFormatter.class);

    @Override
    public byte[] format(Message msg) {
        ObjectMessage objMsg = (ObjectMessage) msg;
        Serializable obj = objMsg.getObject();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        byte[] bytes = null;

        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            bytes = baos.toByteArray();
        } catch (IOException e) {
            bytes = new byte[0];
            logger.error("[format] occurs a IOException : " + e.getMessage());
        } finally {
            try {
                baos.close();
                oos.close();
            } catch (IOException e) {
                logger.error("[format] occurs a IOException in finally block : " + e.getMessage());
            }
        }

        return bytes;
    }

    @Override
    public Message deFormat(byte[] msgBytes) {
        ByteArrayInputStream bais = new ByteArrayInputStream(msgBytes);
        ObjectInputStream objis = null;

        Serializable obj = null;
        try {
            objis = new ObjectInputStream(bais);
            obj = (Serializable) objis.readObject();
        } catch (IOException e) {
            logger.error("[deFormat] occurs a IOException : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.error("[doFormat] occurs a ClassNotFoundException : " + e.getMessage());
        } finally {
            try {
                bais.close();
                objis.close();
            } catch (IOException e) {
                logger.error("[deFormat] occurs a IOException in finally block : " + e.getMessage());
            }
        }

        ObjectMessage objMsg = new ObjectMessagePOJO();
        objMsg.setObject(obj);

        return objMsg;
    }
}
