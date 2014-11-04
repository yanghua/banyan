package com.freedom.managesystem.pojo.rabbitHTTP;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Exchange {

    private String name;
    private String type;
    private String durable;
    private String autoDelete;
    private String internal;

    public Exchange() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDurable() {
        return durable;
    }

    public void setDurable(String durable) {
        this.durable = durable;
    }

    public String getAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(String autoDelete) {
        this.autoDelete = autoDelete;
    }

    public String getInternal() {
        return internal;
    }

    public void setInternal(String internal) {
        this.internal = internal;
    }

    public static Exchange parse(JsonElement exchangeJsonElement) {
        JsonObject exchangeObj = exchangeJsonElement.getAsJsonObject();
        Exchange exchange = new Exchange();
        exchange.setName(exchangeObj.get("name").getAsString());
        exchange.setType(exchangeObj.get("type").getAsString());
        exchange.setDurable(exchangeObj.get("durable").getAsString());
        exchange.setAutoDelete(exchangeObj.get("auto_delete").getAsString());
        exchange.setInternal(exchangeObj.get("internal").getAsString());

        return exchange;
    }
}
