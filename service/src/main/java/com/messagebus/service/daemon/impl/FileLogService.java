package com.messagebus.service.daemon.impl;

import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusPool;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.message.model.Message;
import com.messagebus.service.daemon.DaemonService;
import com.messagebus.service.daemon.RunPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@DaemonService(value = "fileLogService", policy = RunPolicy.ONCE)
public class FileLogService extends AbstractService {

    private static final Log    logger = LogFactory.getLog(FileLogService.class);
    private              String secret = "hkajhdfiuwxjdhakjdshuuuqoxdfasg";

    private MessagebusPool messagebusPool;
    private String         mqHost;
    private int            mqPort;

    public FileLogService(Map<String, Object> context) {
        super(context);

        mqHost = this.context.get(com.messagebus.service.Constants.MQ_HOST_KEY).toString();
        mqPort = Integer.parseInt(this.context.get(com.messagebus.service.Constants.MQ_PORT_KEY).toString());
    }

    @Override
    public void run() {
        messagebusPool = new MessagebusSinglePool(mqHost, mqPort);
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
