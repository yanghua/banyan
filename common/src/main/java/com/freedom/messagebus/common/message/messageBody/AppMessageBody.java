package com.freedom.messagebus.common.message.messageBody;

import com.freedom.messagebus.common.message.IMessageBody;

import java.io.Serializable;

public class AppMessageBody implements IMessageBody, Serializable {

    private byte[] messageBody;

    public AppMessageBody() {
    }

    public byte[] getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(byte[] messageBody) {
        this.messageBody = messageBody;
    }
}
