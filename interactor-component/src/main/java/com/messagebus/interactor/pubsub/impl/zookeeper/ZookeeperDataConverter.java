package com.messagebus.interactor.pubsub.impl.zookeeper;

import com.messagebus.common.ExceptionHelper;
import com.messagebus.interactor.pubsub.IDataConverter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by yanghua on 2/11/15.
 */
public class ZookeeperDataConverter implements IDataConverter {

    private static final Log logger = LogFactory.getLog(ZookeeperDataConverter.class);

    @Override
    public <T> byte[] serialize(Serializable obj) {
        byte[] bytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            oos.flush();

            bytes = baos.toByteArray();
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "serialize");
            throw new RuntimeException(e);
        }

        return bytes;
    }

    @Override
    public <T> T[] deSerializeArray(byte[] originalData, Class<T[]> clazz) {
        Object obj = this.deSerialize(originalData);

        return (T[]) obj;
    }

    @Override
    public <T> T deSerializeObject(byte[] originalData, Class<T> clazz) {
        if (clazz.equals(String.class)) {
            String tmp = new String(originalData, Charset.defaultCharset());
            return (T) tmp;
        }

        Object obj = this.deSerialize(originalData);
        return (T) obj;
    }

    private Object deSerialize(byte[] originalData) {
        if (originalData == null) {
            return null;
        }

        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        Object obj = null;
        try {
            bais = new ByteArrayInputStream(originalData);
            ois = new ObjectInputStream(bais);
            obj = null;
            try {
                obj = ois.readObject();
            } catch (ClassNotFoundException e) {
                logger.error("[download] occurs a ClassNotFoundException : " + e.getMessage());
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            logger.error("occurs a IOException : " + e.toString());
            throw new RuntimeException(e.toString());
        } finally {
            try {
                if (bais != null) bais.close();
                if (ois != null) ois.close();
            } catch (IOException e) {
                logger.error("occurs a IOException : " + e.toString());
            }
        }

        return obj;
    }
}
