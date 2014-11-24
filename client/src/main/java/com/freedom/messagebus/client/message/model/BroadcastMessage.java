package com.freedom.messagebus.client.message.model;

public class BroadcastMessage extends AbstractMessage {

    private BroadcastMessageBody messageBody;

    public BroadcastMessage() {
        this.type = MessageType.BroadcastMessage;
        this.genericMessageHeader.setType(MessageType.BroadcastMessage.getType());
    }

    @Override
    public IMessageBody getMessageBody() {
        return this.messageBody;
    }

    @Override
    public void setMessageBody(IMessageBody messageBody) {
        if (messageBody instanceof BroadcastMessageBody)
            this.messageBody = (BroadcastMessageBody) messageBody;
        else
            throw new ClassCastException("param : messageBody can not be cast to type : BroadcastMessageBody");
    }

    public static class BroadcastMessageBody implements IMessageBody {

        private byte[] content;

        public BroadcastMessageBody() {
        }

        public byte[] getContent() {
            return content;
        }

        public void setContent(byte[] content) {
            this.content = content;
        }
    }
}
