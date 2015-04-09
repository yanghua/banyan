package com.messagebus.server.daemon.impl;

import com.messagebus.business.exchanger.ExchangerManager;
import com.messagebus.client.IRequestListener;
import com.messagebus.client.Messagebus;
import com.messagebus.client.MessagebusPool;
import com.messagebus.client.message.model.Message;
import com.messagebus.client.message.model.MessageFactory;
import com.messagebus.client.message.model.MessageType;
import com.messagebus.common.ExceptionHelper;
import com.messagebus.server.Constants;
import com.messagebus.server.daemon.DaemonService;
import com.messagebus.server.daemon.RunPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@DaemonService(value = "sentinelService", policy = RunPolicy.ONCE)
public class CommandService extends AbstractService {

    private static final Log logger = LogFactory.getLog(CommandService.class);

    private MessagebusPool   messagebusPool;
    private ExchangerManager exchangeManager;

    private String secret = "nadjfqulaudhfkauwaudhfakqajd";

    public CommandService(Map<String, Object> context) {
        super(context);

        messagebusPool = (MessagebusPool) this.context.get(Constants.GLOBAL_CLIENT_POOL);

        this.exchangeManager = (ExchangerManager) this.context.get(Constants.GLOBAL_EXCHANGE_MANAGER);
    }


    @Override
    public void run() {
        Messagebus client = messagebusPool.getResource();
        try {
            client.response(secret, new IRequestListener() {
                @Override
                public Message onRequest(Message requestMsg) {
                    //check command is ping...
                    Map<String, Object> headers = requestMsg.getHeaders();
                    if (logger.isDebugEnabled()) {
                        logger.debug("is header not null : " + (headers != null));
                        logger.debug("is contain COMMAND key : " + (headers.containsKey("COMMAND")));
                        logger.debug("COMMAND value is : " + headers.get("COMMAND"));
                    }

                    boolean baseCheck = (headers != null && headers.containsKey("COMMAND"));

                    Message respMsg = MessageFactory.createMessage(MessageType.QueueMessage);
                    Map<String, Object> respHeader = new HashMap<String, Object>(1);
                    if (baseCheck) {
                        String cmd = headers.get("COMMAND").toString();
                        logger.debug("received " + cmd + " command!");
                        switch (cmd) {
                            case "PING":
                                respHeader.put("COMMAND", "PONG");
                                break;

                            case "INSERT":
                            case "UPDATE":
                            case "DELETE": {
                                if (headers.containsKey("TABLE") && headers.get("TABLE") != null) {
                                    process(headers.get("TABLE").toString());
                                    respMsg.setContent("OK".getBytes());
                                    respHeader.put("COMMAND", cmd);
                                } else {
                                    respMsg.setContent("ERROR".getBytes());
                                    logger.error("received illegal cmd : " + cmd + " TABLE is empty! ");
                                }
                            }
                            break;

                            default: {
                                respMsg.setContent("ERROR".getBytes());
                                logger.error("received unsupported cmd : " + cmd);
                            }
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

    private void process(String tableName) {
        try {
            this.exchangeManager.uploadWithTable(tableName);
        } catch (IOException e) {
            ExceptionHelper.logException(logger, e, "process table : " + tableName);
            throw new RuntimeException(e);
        }
    }

}
