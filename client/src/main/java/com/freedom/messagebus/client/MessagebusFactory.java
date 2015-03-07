package com.freedom.messagebus.client;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * Created by yanghua on 3/5/15.
 */
public class MessagebusFactory implements PooledObjectFactory<Messagebus> {

    private String appId;
    private String pubsuberHost;
    private int pubsuberPort;

    public MessagebusFactory(String appId, String pubsuberHost, int pubsuberPort) {
        this.appId = appId;
        this.pubsuberHost = pubsuberHost;
        this.pubsuberPort = pubsuberPort;
    }

    @Override
    public PooledObject<Messagebus> makeObject() throws Exception {
        Messagebus client = new Messagebus(this.appId);
        client.setPubsuberHost(this.pubsuberHost);
        client.setPubsuberPort(this.pubsuberPort);
        client.open();

        return new DefaultPooledObject<>(client);
    }

    @Override
    public void destroyObject(PooledObject<Messagebus> pooledObject) throws Exception {
        Messagebus client = pooledObject.getObject();
        if (client != null) {
            if (client.isOpen()) {
                client.close();
            }
        }
    }

    @Override
    public boolean validateObject(PooledObject<Messagebus> pooledObject) {
        Messagebus client = pooledObject.getObject();
        return client != null && client.isOpen();
    }

    @Override
    public void activateObject(PooledObject<Messagebus> pooledObject) throws Exception {
        Messagebus client = pooledObject.getObject();
        if (client != null) {
            if (!client.isOpen()) {
                client.open();
            }
        }
    }

    @Override
    public void passivateObject(PooledObject<Messagebus> pooledObject) throws Exception {
        
    }
}
