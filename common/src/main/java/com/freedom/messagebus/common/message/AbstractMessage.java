package com.freedom.messagebus.common.message;

abstract class AbstractMessage implements Message {

    protected MessageType type;
    protected GenericMessageHeader genericMessageHeader;

    protected AbstractMessage() {
        this.genericMessageHeader = new GenericMessageHeader();
    }

    @Override
    public IMessageHeader getMessageHeader() {
        return genericMessageHeader;
    }

    @Override
    public abstract IMessageBody getMessageBody();

    @Override
    public abstract void setMessageBody(IMessageBody messageBody);

    @Override
    public MessageType getMessageType() {
        return this.type;
    }

}
