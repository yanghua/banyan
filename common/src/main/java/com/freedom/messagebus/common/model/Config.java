package com.freedom.messagebus.common.model;

public class Config {

    private String key;
    private String value;

    public Config() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Config{" +
            "key='" + key + '\'' +
            ", value='" + value + '\'' +
            '}';
    }
}
