package com.freedom.messagebus.client.core.message;

/**
 * the text message interface
 */
@Deprecated
public interface TextMessage extends Message {

    /**
     * get real message body
     *
     * @return real message
     */

    public String getMessageBody();

    /**
     * set real message body
     *
     * @param body the real message
     */
    public void setMessageBody(String body);

}
