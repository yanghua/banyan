package com.freedom.messagebus.common.message;

public class Message {

    private IMessageHeader messageHeader;
    private IMessageBody   messageBody;
    private MessageType    messageType;

    public Message() {
    }

    public IMessageHeader getMessageHeader() {
        return messageHeader;
    }

    public void setMessageHeader(IMessageHeader messageHeader) {
        this.messageHeader = messageHeader;
    }

    public IMessageBody getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(IMessageBody messageBody) {
        this.messageBody = messageBody;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}
