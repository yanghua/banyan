package com.messagebus.service.daemon.impl;

import com.messagebus.client.IRequestListener;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusPool;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.service.Constants;
import com.messagebus.service.daemon.DaemonService;
import com.messagebus.service.daemon.RunPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@DaemonService(value = "commandService", policy = RunPolicy.ONCE)
public class CommandService extends AbstractService {

    private static final Log logger = LogFactory.getLog(CommandService.class);

    private MessagebusPool messagebusPool;

    private String secret = "nadjfqulaudhfkauwaudhfakqajd";

    public CommandService(Map<String, Object> context) {
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
                    Map<String, Object> headers = requestMsg.getHeaders();
                    if (logger.isDebugEnabled()) {
                        boolean isHeaderNotNull = (headers != null);
                        logger.debug("is header not null : " + isHeaderNotNull);
                        boolean isContainCmdKey = isHeaderNotNull && headers.containsKey("COMMAND");
                        logger.debug("is contain COMMAND key : " + isContainCmdKey);
                        if (isContainCmdKey) {
                            logger.debug("COMMAND value is : " + headers.get("COMMAND"));
                        }
                    }

                    boolean baseCheck = (headers != null && headers.containsKey("COMMAND"));

                    Message respMsg = MessageFactory.createMessage(MessageType.QueueMessage);
                    Map<String, Object> respHeader = new HashMap<String, Object>(1);

                    if (baseCheck) {
                        String cmd = headers.get("COMMAND").toString();
                        logger.debug("received " + cmd + " command!");
                        if (cmd.equals("PING")) {
                            respHeader.put("COMMAND", "PONG");
                        } else {
                            process(cmd, headers, respHeader);
                        }
                    }

                    respMsg.setHeaders(respHeader);

                    return respMsg;
                }
            }, Integer.MAX_VALUE, TimeUnit.SECONDS);
        } finally {
            messagebusPool.returnResource(client);
        }
    }

    private void process(String cmdName, Map<String, Object> reqHeader, Map<String, Object> respHeader) {

    }

}
