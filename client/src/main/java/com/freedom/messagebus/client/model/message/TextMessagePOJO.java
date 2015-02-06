package com.freedom.messagebus.client.model.message;

import com.freedom.messagebus.client.core.message.TextMessage;

/**
 * the POJO that implements the TextMessage interface
 */
@Deprecated
public class TextMessagePOJO implements TextMessage {

    /**
     * real message body
     */
    private String body;


    @Override
    public String getMessageBody() {
        return body;
    }

    @Override
    public void setMessageBody(String body) {
        this.body = body;
    }
}
