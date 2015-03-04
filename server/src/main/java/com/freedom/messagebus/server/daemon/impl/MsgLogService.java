package com.freedom.messagebus.server.daemon.impl;

import com.freedom.messagebus.client.IMessageReceiveListener;
import com.freedom.messagebus.client.Messagebus;
import com.freedom.messagebus.client.carry.impl.GenericConsumer;
import com.freedom.messagebus.client.message.model.IMessageHeader;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.server.Constants;
import com.freedom.messagebus.server.daemon.DaemonService;
import com.freedom.messagebus.server.daemon.RunPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@DaemonService(value = "msgLogService", policy = RunPolicy.ONCE)
public class MsgLogService extends AbstractService {

    private static final Log logger = LogFactory.getLog(MsgLogService.class);
    private Messagebus      client;
    private GenericConsumer asyncConsumer;

    public MsgLogService(Map<String, Object> context) {
        super(context);

        client = (Messagebus) this.context.get(Constants.GLOBAL_CLIENT_OBJECT);
    }

    @Override
    public void run() {
        client.asyncConsume(
            new IMessageReceiveListener() {
                @Override
                public void onMessage(Message message) {
                    logger.info(formatLog(message.getMessageHeader()));
                }
            }, Integer.MAX_VALUE, TimeUnit.SECONDS);
    }

    private String formatLog(IMessageHeader msgHeader) {
        StringBuilder sb = new StringBuilder();
        sb.append(" [id] ");
        sb.append(msgHeader.getMessageId());
        sb.append(" [type] ");
        sb.append(msgHeader.getType());
        sb.append(" [appId] ");
        sb.append(msgHeader.getAppId());
        sb.append(" [replyTo] ");
        sb.append(msgHeader.getReplyTo());

        return sb.toString();
    }
}
