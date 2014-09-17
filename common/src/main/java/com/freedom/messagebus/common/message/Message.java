package com.freedom.messagebus.common.message;

public class Message {

    private IMessageHeader messageHeader;
    private IMessageBody   messageBody;

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
}
