package com.messagebus.interactor.pubsub;

/**
 * Created by yanghua on 2/6/15.
 */
public interface IPubSuber {

    public void open();

    public void close();

    public boolean isAlive();

    public void watch(String[] channels, IPubSubListener listener);

    public void publish(String channel, byte[] jsonStrBytes);

    public byte[] get(String channel);

    public void setHost(String host);

    public String getHost();

    public void setPort(int port);

    public int getPort();

}
