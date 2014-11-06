package com.freedom.messagebus.server.daemon.impl;

import com.freedom.messagebus.client.*;
import com.freedom.messagebus.common.message.Message;
import com.freedom.messagebus.common.message.MessageFactory;
import com.freedom.messagebus.common.message.MessageType;
import com.freedom.messagebus.common.message.QueueMessage;
import com.freedom.messagebus.server.Constants;
import com.freedom.messagebus.server.daemon.DaemonService;
import com.freedom.messagebus.server.daemon.IService;
import com.freedom.messagebus.server.daemon.RunPolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@DaemonService(value = "sentinelService", policy = RunPolicy.ONCE)
public class SentinelService implements Runnable, IService {

    private static final Log logger = LogFactory.getLog(SentinelService.class);

    private       IReceiverCloser consumerCloser = null;
    private final Object          lockObj        = new Object();
    private Messagebus   client;
    private QueueMessage responseMsg;

    public SentinelService(String host) {
        client = Messagebus.getInstance(Constants.SERVER_APP_ID);
        //set zookeeper info
        client.setZkHost(Constants.ZK_HOST);
        client.setZkPort(Constants.ZK_PORT);

        responseMsg = (QueueMessage) MessageFactory.createMessage(MessageType.QueueMessage);
        Map<String, Object> headers = new HashMap<>(1);
        headers.put("COMMAND", "PONG");
        responseMsg.getMessageHeader().setHeaders(headers);
        responseMsg.getMessageHeader().setAppId(Constants.SERVER_APP_ID);
        responseMsg.getMessageHeader().setReplyTo(Constants.SERVER_APP_ID);
        QueueMessage.QueueMessageBody body = new QueueMessage.QueueMessageBody();
        body.setContent(new byte[0]);
        responseMsg.setMessageBody(body);
    }

    @Override
    public void run() {
        try {
            synchronized (lockObj) {
                client.open();
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
                            logger.debug("is COMMAND equals : " + headers.get("COMMAND").toString().equals("PING"));
                        }

                        boolean isPingCmd = (headers != null && headers.containsKey("COMMAND") &&
                            headers.get("COMMAND").toString().equals("PING"));

                        if (isPingCmd) {
                            logger.debug("received ping command from app id : " + message.getMessageHeader().getAppId());
                            //responseMsg pong
                            responser.responseTmpMessage(responseMsg, msgId);
                        }
                    }
                });

                logger.debug("blocked for receiving message!");
                lockObj.wait(0);
                logger.debug("released object lock!");
            }

        } catch ( MessagebusUnOpenException |
            MessagebusConnectedFailedException | InterruptedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            logger.info("close sentinel");
            consumerCloser.close();
            client.close();
        }
    }

}
