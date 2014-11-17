package com.freedom.messagebus.business.message.model;

import java.security.InvalidParameterException;

public class PubSubMessage extends AbstractMessage {

    private PubSubMessageBody messageBody;

    public PubSubMessage() {
        this.type = MessageType.PubSubMessage;
        this.genericMessageHeader.setType(MessageType.PubSubMessage.getType());
    }

    @Override
    public IMessageBody getMessageBody() {
        return this.messageBody;
    }

    @Override
    public void setMessageBody(IMessageBody messageBody) {
        if (messageBody instanceof PubSubMessageBody)
            this.messageBody = (PubSubMessageBody) messageBody;
        else {
            throw new InvalidParameterException("messageBody should can be cast to : PubSubMessageBody");
        }
    }

    public static class PubSubMessageBody implements IMessageBody {

        private byte[] content;

        public PubSubMessageBody() {
        }

        public byte[] getContent() {
            return content;
        }

        public void setContent(byte[] content) {
            this.content = content;
        }
    }

}
