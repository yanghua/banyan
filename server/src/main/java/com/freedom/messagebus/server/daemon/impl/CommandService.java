package com.freedom.messagebus.server.daemon.impl;

import com.freedom.messagebus.business.exchanger.ExchangerManager;
import com.freedom.messagebus.client.IMessageReceiveListener;
import com.freedom.messagebus.client.Messagebus;
import com.freedom.messagebus.client.message.model.Message;
import com.freedom.messagebus.client.message.model.MessageFactory;
import com.freedom.messagebus.client.message.model.MessageType;
import com.freedom.messagebus.client.message.model.QueueMessage;
import com.freedom.messagebus.server.Constants;
import com.freedom.messagebus.server.daemon.DaemonService;
import com.freedom.messagebus.server.daemon.RunPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@DaemonService(value = "sentinelService", policy = RunPolicy.ONCE)
public class CommandService extends AbstractService {

    private static final Log logger = LogFactory.getLog(CommandService.class);

    private final Object          lockObj        = new Object();

    private Messagebus       client;
    private QueueMessage     responseMsg;
    private Properties       serverConfig;
    private ExchangerManager exchangeManager;

    public CommandService(Map<String, Object> context) {
        super(context);

        serverConfig = (Properties) this.context.get(Constants.KEY_SERVER_CONFIG);

        client = (Messagebus) this.context.get(Constants.GLOBAL_CLIENT_OBJECT);

        responseMsg = (QueueMessage) MessageFactory.createMessage(MessageType.QueueMessage);
        Map<String, Object> headers = new HashMap<>(1);
        headers.put("COMMAND", "PONG");
        responseMsg.getMessageHeader().setHeaders(headers);
        String appId = this.serverConfig.get(Constants.KEY_MESSAGEBUS_SERVER_APP_ID).toString();
        responseMsg.getMessageHeader().setAppId(appId);
        responseMsg.getMessageHeader().setReplyTo(appId);
        QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
        body.setContent(new byte[0]);
        responseMsg.setMessageBody(body);

        this.exchangeManager = (ExchangerManager) this.context.get(Constants.GLOBAL_EXCHANGE_MANAGER);
    }


    @Override
    public void run() {
        synchronized (lockObj) {
            client.consume(
                new IMessageReceiveListener() {
                    @Override
                    public void onMessage(Message message) {
                        String msgId = String.valueOf(message.getMessageHeader().getMessageId());

                        //check command is ping...
                        Map<String, Object> headers = message.getMessageHeader().getHeaders();

                        if (logger.isDebugEnabled()) {
                            logger.debug("msg id is : " + msgId);
                            logger.debug("is header not null : " + (headers != null));
                            logger.debug("is contain COMMAND key : " + (headers.containsKey("COMMAND")));
                            logger.debug("COMMAND value is : " + headers.get("COMMAND"));
                        }

                        boolean baseCheck = (headers != null && headers.containsKey("COMMAND"));

                        if (baseCheck) {
                            String cmd = headers.get("COMMAND").toString();
                            logger.debug("received " + cmd + " command from app id : "
                                             + message.getMessageHeader().getAppId());
                            switch (cmd) {
                                case "PING":
                                    //responseMsg pong
                                    client.responseTmpMessage(responseMsg, msgId);
                                    break;

                                case "INSERT":
                                case "UPDATE":
                                case "DELETE": {
                                    if (headers.containsKey("TABLE") && headers.get("TABLE") != null) {
                                        process(headers.get("TABLE").toString());
                                    } else {
                                        logger.error("received illegal cmd : " + cmd + " TABLE is empty! ");
                                    }
                                }
                                break;

                                default:
                                    logger.error("received unsupported cmd : " + cmd);
                            }
                        }
                    }
                }, Integer.MAX_VALUE, TimeUnit.SECONDS);
        }
    }

    private void process(String tableName) {
        try {
            this.exchangeManager.uploadWithTable(tableName);
        } catch (IOException e) {
            logger.error("[process] occurs a IOException : " + e.getMessage());
        }
    }

}
