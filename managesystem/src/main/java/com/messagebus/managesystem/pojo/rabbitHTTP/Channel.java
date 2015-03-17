package com.messagebus.managesystem.pojo.rabbitHTTP;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Channel {

    private String name;
    private String userName;
    private String transactional;
    private String confirm;
    private long   prefetch;
    private long   unacked;
    private long   unconfirmed;
    private String state;

    public Channel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String isTransactional() {
        return transactional;
    }

    public void setTransactional(String transactional) {
        this.transactional = transactional;
    }

    public String isConfirm() {
        return confirm;
    }

    public void setConfirm(String confirm) {
        this.confirm = confirm;
    }

    public long getPrefetch() {
        return prefetch;
    }

    public void setPrefetch(long prefetch) {
        this.prefetch = prefetch;
    }

    public long getUnacked() {
        return unacked;
    }

    public void setUnacked(long unacked) {
        this.unacked = unacked;
    }

    public long getUnconfirmed() {
        return unconfirmed;
    }

    public void setUnconfirmed(long unconfirmed) {
        this.unconfirmed = unconfirmed;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public static Channel parse(JsonElement channelJsonElement) {
        JsonObject channelObj = channelJsonElement.getAsJsonObject();
        Channel channel = new Channel();
        channel.setName(channelObj.get("name").getAsString());
        channel.setUserName(channelObj.get("user").getAsString());
        channel.setTransactional(channelObj.get("transactional").getAsString());
        channel.setConfirm(channelObj.get("confirm").getAsString());
        channel.setPrefetch(channelObj.get("prefetch_count").getAsLong());
        channel.setState(channelObj.get("state").getAsString());
        channel.setUnacked(channelObj.get("messages_unacknowledged").getAsLong());
        channel.setUnconfirmed(channelObj.get("messages_unconfirmed").getAsLong());

        return channel;
    }

}