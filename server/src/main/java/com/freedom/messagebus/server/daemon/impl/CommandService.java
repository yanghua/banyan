package com.freedom.messagebus.server.daemon.impl;

import com.freedom.messagebus.client.*;
import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.common.message.MessageFactory;
import com.freedom.messagebus.common.message.MessageType;
import com.freedom.messagebus.common.message.QueueMessage;
import com.freedom.messagebus.interactor.zookeeper.LongLiveZookeeper;
import com.freedom.messagebus.server.Constants;
import com.freedom.messagebus.server.daemon.DaemonService;
import com.freedom.messagebus.server.daemon.RunPolicy;
import com.freedom.messagebus.server.dataaccess.DBAccessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@DaemonService(value = "sentinelService", policy = RunPolicy.ONCE)
public class CommandService extends AbstractService {

    private static final Log logger = LogFactory.getLog(CommandService.class);

    private       IReceiverCloser consumerCloser = null;
    private final Object          lockObj        = new Object();

    private Messagebus        client;
    private QueueMessage      responseMsg;
    private LongLiveZookeeper zookeeper;
    private Properties        serverConfig;

    public CommandService(Map<String, Object> context) {
        super(context);

        serverConfig = (Properties) this.context.get(Constants.KEY_SERVER_CONFIG);

        //set zookeeper info
        client = (Messagebus) this.context.get(Constants.GLOBAL_CLIENT_OBJECT);

        responseMsg = (QueueMessage) MessageFactory.createMessage(MessageType.QueueMessage);
        Map<String, Object> headers = new HashMap<>(1);
        headers.put("COMMAND", "PONG");
        responseMsg.getMessageHeader().setHeaders(headers);
        responseMsg.getMessageHeader().setAppId(Constants.SERVER_APP_ID);
        responseMsg.getMessageHeader().setReplyTo(Constants.SERVER_APP_ID);
        QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
        body.setContent(new byte[0]);
        responseMsg.setMessageBody(body);

        zookeeper = (LongLiveZookeeper) context.get(Constants.GLOBAL_ZOOKEEPER_OBJECT);
    }


    @Override
    public void run() {
        try {
            synchronized (lockObj) {
                IConsumer consumer = client.getConsumer();
                final IResponser responser = client.getResponser();
                consumerCloser = consumer.consume(Constants.SERVER_QUEUE_NAME, new IMessageReceiveListener() {
                    @Override
                    public void onMessage(Message message, IReceiverCloser consumerCloser) {
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
                                    responser.responseTmpMessage(responseMsg, msgId);
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
                });

                logger.debug("blocked for receiving message!");
                lockObj.wait(0);
                logger.debug("released object lock!");
            }

        } catch (MessagebusUnOpenException | InterruptedException e) {
            logger.error("[run] occurs a Exception : " + e.getMessage());
        } catch (Exception e) {
            logger.error("[run] occurs a Exception : " + e.getMessage());
        } finally {
            logger.info("close sentinel");
            consumerCloser.close();
        }
    }

    private void process(String tableName) {
        switch (tableName) {
            case "NODE":
                this.processNode();
                break;

            case "CONFIG":
                this.processConfig();
                break;

            default:
                logger.error("[process] unsupported table name : " + tableName);
        }
    }

    private void processNode() {
        DBAccessor dbAccessor = DBAccessor.defaultAccessor(serverConfig);
        try {
            dbAccessor.dumpDbInfo(CONSTS.EXPORTED_NODE_CMD_FORMAT, CONSTS.EXPORTED_NODE_FILE_PATH);
            setDbInfoToZK(CONSTS.EXPORTED_NODE_FILE_PATH, CONSTS.ZOOKEEPER_ROOT_PATH_FOR_ROUTER);
        } catch (IOException e) {
            logger.error("[processNode] occurs a IOException : " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("[processNode] occurs a InterruptedException : " + e.getMessage());
        }
    }

    private void processConfig() {
        Properties serverConfig = (Properties) this.context.get(Constants.KEY_SERVER_CONFIG);
        DBAccessor dbAccessor = DBAccessor.defaultAccessor(serverConfig);
        try {
            dbAccessor.dumpDbInfo(CONSTS.EXPORTED_CONFIG_CMD_FORMAT, CONSTS.EXPORTED_CONFIG_FILE_PATH);
            setDbInfoToZK(CONSTS.EXPORTED_CONFIG_FILE_PATH, CONSTS.ZOOKEEPER_ROOT_PATH_FOR_CONFIG);
        } catch (IOException e) {
            logger.error("[processNode] occurs a IOException : " + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("[processNode] occurs a InterruptedException : " + e.getMessage());
        }
    }

    private void setDbInfoToZK(String filePath, String zkNode) throws IOException {
        FileReader reader = new FileReader(filePath);
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();

        String tmp = null;
        while ((tmp = bufferedReader.readLine()) != null) {
            sb.append(tmp);
        }

        String totalStr = sb.toString();
        this.zookeeper.setConfig(zkNode,
                                 totalStr.getBytes(),
                                 true);
    }
}
