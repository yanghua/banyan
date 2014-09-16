package com.freedom.messagebus.common.message.messageBody;

import com.freedom.messagebus.common.message.IMessageBody;

import java.io.Serializable;

public class CacheExpiredMessageBody implements IMessageBody,Serializable {

    private String cache;

    public CacheExpiredMessageBody() {
    }

    public String getCache() {
        return cache;
    }

    public void setCache(String cache) {
        this.cache = cache;
    }
}
