package com.messagebus.client.message.model;

public interface IMessage {

    public IMessageHeader getMessageHeader();

    public IMessageBody getMessageBody();

    public void setMessageBody(IMessageBody messageBody);

    public MessageType getMessageType();

}
