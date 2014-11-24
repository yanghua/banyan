package com.freedom.messagebus.client;


import com.freedom.messagebus.client.message.model.Message;
import org.jetbrains.annotations.NotNull;

public interface IResponser {

    /**
     * response a temp message to a named queue
     *
     * @param msg       the entity of message
     * @param queueName the temp queue name
     */
    public void responseTmpMessage(@NotNull Message msg, @NotNull String queueName);

}
