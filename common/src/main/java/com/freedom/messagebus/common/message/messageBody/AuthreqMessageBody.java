package com.freedom.messagebus.common.message.messageBody;

import com.freedom.messagebus.common.message.IMessageBody;

import java.io.Serializable;

public class AuthreqMessageBody implements IMessageBody,Serializable {

    private String appId;

    public AuthreqMessageBody() {
    }
}
