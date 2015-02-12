package com.freedom.messagebus.interactor.pubsub.impl.redis;

import com.freedom.messagebus.interactor.pubsub.IDataConverter;
import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yanghua on 2/11/15.
 */
public class RedisDataConverter implements IDataConverter {

    private static final Log logger = LogFactory.getLog(RedisDataConverter.class);

    private static final Gson gson = new Gson();

    @Override
    public <T> byte[] serialize(Serializable obj) {
        String tmp;
        if (obj instanceof String) {
            tmp = (String) obj;
        } else if (obj instanceof List) {
            tmp = gson.toJson(obj, List.class);
        } else {
            tmp = gson.toJson(obj);
        }

        return tmp.getBytes();
    }

    @Override
    public <T> T deSerializeObject(byte[] originalData, Class<T> clazz) {
        String jsonStr = new String(originalData);
        return gson.fromJson(jsonStr, clazz);
    }

    @Override
    public <T> T[] deSerializeArray(byte[] originalData, Class<T[]> clazz) {
        String jsonStr = new String(originalData);
        return gson.fromJson(jsonStr, clazz);
    }
}
