package com.freedom.messagebus.common.message.messageBody;

import com.freedom.messagebus.common.message.IMessageBody;

import java.io.Serializable;

public class AuthrespMessageBody implements IMessageBody, Serializable {

    private String result;

    public AuthrespMessageBody() {
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
