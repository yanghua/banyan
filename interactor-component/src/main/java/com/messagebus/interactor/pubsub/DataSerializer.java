package com.messagebus.interactor.pubsub;

import com.google.gson.Gson;
import com.messagebus.common.ExceptionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by yanghua on 2/11/15.
 */
public class DataSerializer {

    private static final Log logger = LogFactory.getLog(DataSerializer.class);

    private static final Gson gson = new Gson();

    public <T> byte[] serialize(Serializable obj) {
        String tmp;
        if (obj instanceof String) {
            tmp = (String) obj;
        } else if (obj instanceof List) {
            tmp = gson.toJson(obj, List.class);
        } else {
            tmp = gson.toJson(obj);
        }

        return tmp.getBytes(Charset.defaultCharset());
    }


    public <T> byte[] serialize(Serializable obj, Class<T> clazz) {
        String tmp;
        tmp = gson.toJson(obj, clazz);
        return tmp.getBytes(Charset.defaultCharset());
    }

    public <T> T deSerializeObject(byte[] originalData, Class<T> clazz) {
        if (originalData == null || originalData.length == 0) {
            try {
                return clazz.newInstance();
            } catch (InstantiationException e) {
                ExceptionHelper.logException(logger, e, "");
            } catch (IllegalAccessException e) {
                ExceptionHelper.logException(logger, e, "");
            }
        }

        String jsonStr = new String(originalData, Charset.defaultCharset());
        return gson.fromJson(jsonStr, clazz);
    }

    public <T> T[] deSerializeArray(byte[] originalData, Class<T[]> clazz) {
        String jsonStr = new String(originalData, Charset.defaultCharset());
        return gson.fromJson(jsonStr, clazz);
    }
}
