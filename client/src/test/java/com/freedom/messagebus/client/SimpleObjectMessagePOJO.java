package com.freedom.messagebus.client;

import java.io.Serializable;

/**
 * User: yanghua
 * Date: 7/22/14
 * Time: 4:15 PM
 * Copyright (c) 2013 yanghua. All rights reserved.
 */
public class SimpleObjectMessagePOJO implements Serializable {

    private String txt;

    public SimpleObjectMessagePOJO() {
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    @Override
    public String toString() {
        return "SimpleObjectMessagePOJO{" +
            "txt='" + txt + '\'' +
            '}';
    }
}
