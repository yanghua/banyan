package com.freedom.messagebus.client.message.model;

public interface Message {

    public IMessageHeader getMessageHeader();

    public IMessageBody getMessageBody();

    public void setMessageBody(IMessageBody messageBody);

    public MessageType getMessageType();

}
