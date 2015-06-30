package com.messagebus.interactor.pubsub;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class PubsuberManager {

    private static final Log logger = LogFactory.getLog(PubsuberManager.class);

    private Map<String, IPubSubListener> registry;
    private IPubSuber                    pubsuber;
    private IDataConverter               pubsuberDataConverter;

    public PubsuberManager(String pubsuberHost, int pubsuberPort) {
        this.pubsuber = PubSuberFactory.createPubSuber();
        this.pubsuber.setHost(pubsuberHost);
        this.pubsuber.setPort(pubsuberPort);
        this.pubsuber.open();
        this.pubsuberDataConverter = PubSuberFactory.createConverter();

        boolean isAlive = this.pubsuber.isAlive();
        if (!isAlive)
            return;

        registry = new ConcurrentHashMap<String, IPubSubListener>();
    }

    public boolean isPubsuberAlive() {
        return this.pubsuber.isAlive();
    }

    public synchronized void set(String key, Serializable data) {
        byte[] serializedData = this.pubsuberDataConverter.serialize(data);
        this.pubsuber.set(key, serializedData);
    }

    public synchronized void set(String key, Serializable data, Class<?> clazz) {
        byte[] serializedData = this.pubsuberDataConverter.serialize(data, clazz);
        this.pubsuber.set(key, serializedData);
    }

    public synchronized <T> T get(String key, Class<T> clazz) {
        byte[] originalData = this.pubsuber.get(key);
        return this.pubsuberDataConverter.deSerializeObject(originalData, clazz);
    }

    public synchronized boolean exists(String key) {
        return this.pubsuber.exists(key);
    }

    public synchronized void publish(String channel, byte[] data) {
        this.pubsuber.publish(channel, data);
    }

    public synchronized void publish(String channel, byte[] data, boolean setByHand) {
        if (setByHand) {
            this.pubsuber.set(channel, data);
        }
        this.pubsuber.publish(channel, data);
    }

    public byte[] serialize(Serializable obj, Class<?> clazz) {
        return this.pubsuberDataConverter.serialize(obj, clazz);
    }

    public <T> T deserialize(byte[] originalData, Class<T> clazz) {
        return this.pubsuberDataConverter.deSerializeObject(originalData, clazz);
    }

    public void destroy() {
        if (this.pubsuber != null && this.pubsuber.isAlive())
            this.pubsuber.close();
    }

    private void registerWithChannel(String channel, IPubSubListener handler) {
        if (!registry.containsKey(channel)) {
            registry.put(channel, handler);
        }
    }

    public void registerWithMultiChannels(Map<String, IPubSubListener> channelHandlerMap) {
        List<String> channels = new ArrayList<String>(channelHandlerMap.size());
        for (Map.Entry<String, IPubSubListener> channelHandler : channelHandlerMap.entrySet()) {
            this.registerWithChannel(channelHandler.getKey(), channelHandler.getValue());
            channels.add(channelHandler.getKey());
        }

        this.watchPubSuber(channels);
    }

    private void watchPubSuber(List<String> channels) {
        this.pubsuber.watch(channels.toArray(new String[channels.size()]), new IPubSubListener() {

            public void onChange(String channel, byte[] data, Map<String, Object> params) {
                IPubSubListener handler = registry.get(channel);
                if (handler == null) {
                    logger.warn("channel : " + channel + "occurs a event but can not get handler ! ");
                } else {
                    handler.onChange(channel, data, params);
                }
            }
        });
    }

}
