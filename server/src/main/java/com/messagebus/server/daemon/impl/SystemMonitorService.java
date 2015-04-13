package com.messagebus.server.daemon.impl;

import com.messagebus.client.IRequestListener;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusPool;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.common.HttpHelper;
import com.messagebus.server.Constants;
import com.messagebus.server.daemon.DaemonService;
import com.messagebus.server.daemon.RunPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@DaemonService(value = "systemMonitorService", policy = RunPolicy.ONCE)
public class SystemMonitorService extends AbstractService {

    private static final Log    logger = LogFactory.getLog(SystemMonitorService.class);
    private static final String secret = "iqwjasdfklakqoiajsidfoasidjoqw";

    private MessagebusPool messagebusPool;

    public SystemMonitorService(Map<String, Object> context) {
        super(context);

        messagebusPool = (MessagebusPool) this.context.get(Constants.GLOBAL_CLIENT_POOL);
    }

    @Override
    public void run() {
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
        requestParamDic.put("host", Constants.HOST);
        requestParamDic.put("port", Constants.PORT);
        requestParamDic.put("path", Constants.HTTP_API_OVERVIEW);
        return HttpHelper.syncHTTPGet(requestParamDic, Constants.DEFAULT_AUTH_INFO);
    }
}
