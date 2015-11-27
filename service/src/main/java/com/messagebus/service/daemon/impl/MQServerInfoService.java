package com.messagebus.service.daemon.impl;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.messagebus.client.IRequestListener;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusPool;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.common.HttpHelper;
import com.messagebus.service.Constants;
import com.wisedu.astraea.configuration.LongLiveZookeeper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//@DaemonService(value = "mqServerInfoService", policy = RunPolicy.ONCE)
public class MQServerInfoService extends AbstractService {

    private static final Log    logger                    = LogFactory.getLog(MQServerInfoService.class);
    private static final String secret                    = "iqwjasdfklakqoiajsidfoasidjoqw";
    private static final String COMPONENT_MESSAGE_ZK_PATH = "/component/message";
    private static final Gson   GSON                      = new Gson();

    private Map mbHostAndPortObj;

    public MQServerInfoService(Map<String, Object> context) {
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
            client.response(secret, new IRequestListener() {
                @Override
                public Message onRequest(Message requestMsg) {
                    String rabbitmqServerInfo = getRabbitmqServerInfo();
                    Message respMsg = MessageFactory.createMessage(MessageType.QueueMessage);
                    respMsg.setContent(rabbitmqServerInfo.getBytes());

                    return respMsg;
                }
            }, Integer.MAX_VALUE, TimeUnit.SECONDS);
        } finally {
            messagebusPool.returnResource(client);
        }
    }

    private String getRabbitmqServerInfo() {
        Map<String, Object> requestParamDic = new HashMap<String, Object>(3);
        requestParamDic.put("host", mbHostAndPortObj.get("mqPort").toString());
        requestParamDic.put("port", new Float(mbHostAndPortObj.get("mqPort").toString()).intValue());
        requestParamDic.put("path", Constants.HTTP_API_OVERVIEW);
        return HttpHelper.syncHTTPGet(requestParamDic, Constants.DEFAULT_AUTH_INFO);
    }
}
