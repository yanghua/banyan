package com.messagebus.client;

import java.io.Serializable;

/**
 * Created by yanghua on 2/12/2015.
 */
class Address implements Serializable {

    private String host;
    private int    port;

    public Address() {
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
