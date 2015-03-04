package com.freedom.messagebus.client.carry;


import com.freedom.messagebus.client.message.model.Message;

public interface IResponser {

    /**
     * response a temp message to a named queue
     *
     * @param msg       the entity of message
     * @param queueName the temp queue name
     */
    public void responseTmpMessage(Message msg, String queueName);

}
