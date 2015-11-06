package com.messagebus.service.daemon.impl;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.messagebus.client.IMessageReceiveListener;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusPool;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.message.model.Message;
import com.messagebus.interactor.pubsub.LongLiveZookeeper;
import com.messagebus.service.Constants;
import com.messagebus.service.daemon.DaemonService;
import com.messagebus.service.daemon.RunPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@DaemonService(value = "fileLogService", policy = RunPolicy.ONCE)
public class FileLogService extends AbstractService {

    private static final Log    logger                    = LogFactory.getLog(FileLogService.class);
    private              String secret                    = "hkajhdfiuwxjdhakjdshuuuqoxdfasg";
    private static final String COMPONENT_MESSAGE_ZK_PATH = "/component/message";

    private Map mbHostAndPortObj;

    public FileLogService(Map<String, Object> context) {
        super(context);

        String zkHost = context.get(Constants.ZK_HOST_KEY).toString();
        int zkPort = Integer.parseInt(context.get(Constants.ZK_PORT_KEY).toString());

        LongLiveZookeeper zookeeper = new LongLiveZookeeper(zkHost, zkPort);
        try {
            zookeeper.open();

            mbHostAndPortObj = zookeeper.get(COMPONENT_MESSAGE_ZK_PATH, Map.class);
        } catch (JsonSyntaxException e) {
            logger.error(e);
            throw new RuntimeException(e);
        } finally {
            if (zookeeper.isAlive()) {
                zookeeper.close();
            }
        }
    }

    @Override
    public void run() {
        String mqHost = mbHostAndPortObj.get("mqHost").toString();
        int mqPort = new Float(mbHostAndPortObj.get("mqPort").toString()).intValue();

        MessagebusPool messagebusPool = new MessagebusSinglePool(mqHost, mqPort);
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
