package com.messagebus.client.message.model;

import java.security.InvalidParameterException;

public class Message extends AbstractMessage {

    private MessageBody body;

    public Message() {
        this.type = MessageType.QueueMessage;
        this.genericMessageHeader.setType(MessageType.QueueMessage.getType());
    }

    @Override
    public IMessageBody getMessageBody() {
        return this.body;
    }

    @Override
    public void setMessageBody(IMessageBody messageBody) {
        if (messageBody instanceof MessageBody)
            this.body = (MessageBody) messageBody;
        else {
            throw new InvalidParameterException("messageBody should can be cast to : "
                                                    + MessageBody.class.getName());
        }
    }

    public static class MessageBody implements IMessageBody {

        private byte[] content;

        public MessageBody() {
        }

        public byte[] getContent() {
            return content;
        }

        public void setContent(byte[] content) {
            this.content = content;
        }
    }
}
