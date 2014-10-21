package com.freedom.messagebus.common.message;

public interface Message {

    public IMessageHeader getMessageHeader();

    public IMessageBody getMessageBody();

    public void setMessageBody(IMessageBody messageBody);

    public MessageType getMessageType();

}
