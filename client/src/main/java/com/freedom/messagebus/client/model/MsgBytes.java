package com.freedom.messagebus.client.model;

/**
 * Desc: note that this class just for warp the field 'msgBytes'
 * because of the rabbitmq's publish api just receive the type: <code>byte[]</code>,
 * but if we must encapsulate a api like:
 * batchProduce(Message[] msgs, ...)
 * for some reason, we must get a collection of each message's byte array(from json string)
 * so, it seems that we just only can choose List<Byte[]>, but in java, there is no way changing between
 * byte[] and Byte[] automatically. What's more the type-cast is expensive.
 * So, we warped byte[] to a object and let the list store MsgBytes.
 */
public class MsgBytes extends BaseModel {

    private byte[] msgBytes;

    public MsgBytes() {
    }

    public byte[] getMsgBytes() {
        return msgBytes;
    }

    public void setMsgBytes(byte[] msgBytes) {
        this.msgBytes = msgBytes;
    }

    @Override
    public String toString() {
        return "MsgBytes";
    }
}
