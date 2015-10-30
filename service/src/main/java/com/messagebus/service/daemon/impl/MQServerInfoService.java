package com.messagebus.service.daemon.impl;

import com.messagebus.client.IRequestListener;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusPool;
import com.messagebus.client.MessagebusSinglePool;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.common.HttpHelper;
import com.messagebus.service.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

//@DaemonService(value = "mqServerInfoService", policy = RunPolicy.ONCE)
public class MQServerInfoService extends AbstractService {

    private static final Log    logger = LogFactory.getLog(MQServerInfoService.class);
    private static final String secret = "iqwjasdfklakqoiajsidfoasidjoqw";

    private String mqHost;
    private int    mqPort;

    public MQServerInfoService(Map<String, Object> context) {
        super(context);

        mqHost = this.context.get(com.messagebus.service.Constants.MQ_HOST_KEY).toString();
        mqPort = Integer.parseInt(this.context.get(com.messagebus.service.Constants.MQ_PORT_KEY).toString());
    }

    @Override
    public void run() {
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
        requestParamDic.put("host", context.get(Constants.MQ_HOST_KEY).toString());
        requestParamDic.put("port", Constants.PORT);
        requestParamDic.put("path", Constants.HTTP_API_OVERVIEW);
        return HttpHelper.syncHTTPGet(requestParamDic, Constants.DEFAULT_AUTH_INFO);
    }
}
