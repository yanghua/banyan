package com.freedom.messagebus.client.formatter;

import com.freedom.messagebus.client.core.message.Message;
import com.freedom.messagebus.client.core.message.TextMessage;
import com.freedom.messagebus.client.model.message.TextMessagePOJO;
import com.google.gson.Gson;

/**
 * the default text formatter
 */
@Deprecated
public class DefaultTextFormatter implements IFormatter {

    //TODO: choose json or bson by config
    private static final Gson gson = new Gson();
//    private static final ObjectMapper mapper = new ObjectMapper(new BsonFactory());

    @Override
    public byte[] format(Message msg) {
        TextMessage txtMsg = (TextMessage) msg;
        String content = txtMsg.getMessageBody();

        return content.getBytes();
    }

    @Override
    public Message deFormat(byte[] msgBytes) {
        String msg = new String(msgBytes);
        TextMessage txtMsg = new TextMessagePOJO();
        txtMsg.setMessageBody(msg);

        return txtMsg;
    }
}
