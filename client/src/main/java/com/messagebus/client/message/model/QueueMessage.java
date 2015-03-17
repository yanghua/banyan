package com.messagebus.client.message.model;

import java.security.InvalidParameterException;

public class QueueMessage extends AbstractMessage {

    private QueueMessageBody body;

    public QueueMessage() {
        this.type = MessageType.QueueMessage;
        this.genericMessageHeader.setType(MessageType.QueueMessage.getType());
    }

    @Override
    public IMessageBody getMessageBody() {
        return this.body;
    }

    @Override
    public void setMessageBody(IMessageBody messageBody) {
        if (messageBody instanceof QueueMessageBody)
            this.body = (QueueMessageBody) messageBody;
        else {
            throw new InvalidParameterException("messageBody should can be cast to : "
                                                    + QueueMessageBody.class.getName());
        }
    }

    public static class QueueMessageBody implements IMessageBody {

        private byte[] content;

        public QueueMessageBody() {
        }

        public byte[] getContent() {
            return content;
        }

        public void setContent(byte[] content) {
            this.content = content;
        }
    }
}
