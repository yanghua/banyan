package com.freedom.messagebus.business.exchanger.impl;

import com.freedom.messagebus.business.exchanger.IDataExchanger;
import com.freedom.messagebus.business.exchanger.IDataFetcher;
import com.freedom.messagebus.interactor.zookeeper.LongLiveZookeeper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

public abstract class AbstractExchanger implements IDataExchanger {

    private static final Log logger = LogFactory.getLog(AbstractExchanger.class);

    protected byte[] serializedBytes;

    protected LongLiveZookeeper zookeeper;
    protected String            zkPath;

    public IDataFetcher dataFetcher;

    public AbstractExchanger(LongLiveZookeeper zookeeper, String zkPath) {
        this.zookeeper = zookeeper;
        this.zkPath = zkPath;

        try {
            this.zookeeper.createNode(this.zkPath);
        } catch (Exception e) {
            logger.error("[constructor] occurs a Exception : " + e.getMessage());
        }
    }

    protected byte[] serialize(Serializable obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.flush();

        byte[] bytes = baos.toByteArray();
        baos.close();
        oos.close();

        return bytes;
    }

    protected Object deSerialize(byte[] originalData) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(originalData);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object obj = null;
        try {
            obj = ois.readObject();
        } catch (ClassNotFoundException e) {
            logger.error("[download] occurs a ClassNotFoundException : " + e.getMessage());
            throw new RuntimeException(e);
        }
        bais.close();
        ois.close();

        return obj;
    }

    @Override
    public void upload(Serializable obj) throws IOException {
        this.serializedBytes = this.serialize(obj);
    }

}
