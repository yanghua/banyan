package com.messagebus.service.daemon.impl;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusPool;
import com.messagebus.client.message.model.Message;
import com.messagebus.service.Constants;
import com.messagebus.service.daemon.DaemonService;
import com.messagebus.service.daemon.RunPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@DaemonService(value = "msgLogService", policy = RunPolicy.ONCE)
public class MsgLogService extends AbstractService {

    private static final Log    logger = LogFactory.getLog(MsgLogService.class);
    private              String secret = "hkajhdfiuwxjdhakjdshuuuqoxdfasg";
    private MessagebusPool messagebusPool;

    public MsgLogService(Map<String, Object> context) {
        super(context);

        messagebusPool = (MessagebusPool) this.context.get(Constants.GLOBAL_CLIENT_POOL);
    }

    @Override
    public void run() {
        Messagebus client = messagebusPool.getResource();
        try {
            client.consume(secret, Integer.MAX_VALUE, TimeUnit.SECONDS,
                           new IMessageReceiveListener() {
                               @Override
                               public void onMessage(Message message) {
                                   logger.info(formatLog(message));
                               }
                           });
        } finally {
            messagebusPool.returnResource(client);
        }
    }

    private String formatLog(Message msg) {
        StringBuilder sb = new StringBuilder();
        sb.append(" [id] ");
        sb.append(msg.getMessageId());
        sb.append(" [type] ");
        sb.append(msg.getType());
        sb.append(" [replyTo] ");
        sb.append(msg.getReplyTo());

        return sb.toString();
    }
}
