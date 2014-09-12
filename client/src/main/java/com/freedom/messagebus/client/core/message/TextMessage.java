package com.freedom.messagebus.client.core.message;

import org.jetbrains.annotations.NotNull;

/**
 * the text message interface
 */
public interface TextMessage extends Message {

    /**
     * get real message body
     *
     * @return real message
     */
    @NotNull
    public String getMessageBody();

    /**
     * set real message body
     *
     * @param body the real message
     */
    public void setMessageBody(@NotNull String body);

}
