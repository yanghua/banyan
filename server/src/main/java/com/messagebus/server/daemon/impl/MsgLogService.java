package com.messagebus.server.daemon.impl;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.Messagebus;
import com.messagebus.client.message.model.Message;
import com.messagebus.server.Constants;
import com.messagebus.server.daemon.DaemonService;
import com.messagebus.server.daemon.RunPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@DaemonService(value = "msgLogService", policy = RunPolicy.ONCE)
public class MsgLogService extends AbstractService {

    private static final Log    logger = LogFactory.getLog(MsgLogService.class);
    private              String secret = "hkajhdfiuwxjdhakjdshuuuqoxdfasg";
    private Messagebus client;

    public MsgLogService(Map<String, Object> context) {
        super(context);

        client = (Messagebus) this.context.get(Constants.GLOBAL_CLIENT_OBJECT);
    }

    @Override
    public void run() {
        client.consume(secret, Integer.MAX_VALUE, TimeUnit.SECONDS,
                       new IMessageReceiveListener() {
                           @Override
                           public void onMessage(Message message) {
                               logger.info(formatLog(message));
                           }
                       });
    }

    private String formatLog(Message msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(" [id] ");
        sb.append(msg.getMessageId());
        sb.append(" [type] ");
        sb.append(msg.getType());
        sb.append(" [appId] ");
        sb.append(msg.getAppId());
        sb.append(" [replyTo] ");
        sb.append(msg.getReplyTo());

        return sb.toString();
    }
}
