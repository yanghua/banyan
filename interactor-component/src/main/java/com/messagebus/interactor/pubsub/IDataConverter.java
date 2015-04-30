package com.messagebus.interactor.pubsub;

import java.io.Serializable;

/**
 * Created by yanghua on 2/11/15.
 */
public interface IDataConverter {

    public <T> byte[] serialize(Serializable obj);

    public <T> byte[] serialize(Serializable obj, Class<T> clazz);

    public <T> T deSerializeObject(byte[] originalData, Class<T> clazz);

    public <T> T[] deSerializeArray(byte[] originalData, Class<T[]> clazz);

}
