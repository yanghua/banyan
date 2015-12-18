//package com.messagebus.service.daemon.impl;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonSyntaxException;
//import com.messagebus.client.IRequestListener;
//import com.messagebus.client.Messagebus;
//import com.messagebus.client.MessagebusPool;
//import com.messagebus.client.MessagebusSinglePool;
//import com.messagebus.client.message.model.Message;
//import com.messagebus.client.message.model.MessageFactory;
//import com.messagebus.service.configuration.LongLiveZookeeper;
//import com.messagebus.service.Constants;
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//
////@DaemonService(value = "commandService", policy = RunPolicy.ONCE)
//public class CommandService extends AbstractService {
//
//    private static final Log    logger                    = LogFactory.getLog(CommandService.class);
//    private static final String COMPONENT_MESSAGE_ZK_PATH = "/component/message";
//    private static final Gson   GSON                      = new Gson();
//
//    private Map mbHostAndPortObj;
//
//    private String secret = "nadjfqulaudhfkauwaudhfakqajd";
//
//    public CommandService(Map<String, Object> context) {
//        super(context);
//
//        String zkHost = context.get(Constants.ZK_HOST_KEY).toString();
//        int zkPort = Integer.parseInt(context.get(Constants.ZK_PORT_KEY).toString());
//
//        LongLiveZookeeper zookeeper = new LongLiveZookeeper(zkHost, zkPort);
//        try {
//            zookeeper.open();
//
//            mbHostAndPortObj = zookeeper.get(COMPONENT_MESSAGE_ZK_PATH, Map.class);
//        } catch (JsonSyntaxException e) {
//            logger.error(e);
//            throw new RuntimeException(e);
//        } finally {
//            if (zookeeper.isAlive()) {
//                zookeeper.close();
//            }
//        }
//    }
//
//    @Override
//    public void run() {
//        String mqHost = mbHostAndPortObj.get("mqHost").toString();
//        int mqPort = new Float(mbHostAndPortObj.get("mqPort").toString()).intValue();
//
//        MessagebusPool messagebusPool = new MessagebusSinglePool(mqHost, mqPort);
//        Messagebus client = messagebusPool.getResource();
//        try {
//            client.response(secret, new IRequestListener() {
//                @Override
//                public Message onRequest(Message requestMsg) {
//                    Map<String, Object> headers = requestMsg.getHeaders();
//                    if (logger.isDebugEnabled()) {
//                        boolean isHeaderNotNull = (headers != null);
//                        logger.debug("is header not null : " + isHeaderNotNull);
//                        boolean isContainCmdKey = isHeaderNotNull && headers.containsKey("COMMAND");
//                        logger.debug("is contain COMMAND key : " + isContainCmdKey);
//                        if (isContainCmdKey) {
//                            logger.debug("COMMAND value is : " + headers.get("COMMAND"));
//                        }
//                    }
//
//                    boolean baseCheck = (headers != null && headers.containsKey("COMMAND"));
//
//                    Message respMsg = MessageFactory.createMessage();
//                    Map<String, Object> respHeader = new HashMap<String, Object>(1);
//
//                    if (baseCheck) {
//                        String cmd = headers.get("COMMAND").toString();
//                        logger.debug("received " + cmd + " command!");
//                        if (cmd.equals("PING")) {
//                            respHeader.put("COMMAND", "PONG");
//                        } else {
//                            process(cmd, headers, respHeader);
//                        }
//                    }
//
//                    respMsg.setHeaders(respHeader);
//
//                    return respMsg;
//                }
//            }, Integer.MAX_VALUE, TimeUnit.SECONDS);
//        } finally {
//            messagebusPool.returnResource(client);
//        }
//    }
//
//    private void process(String cmdName, Map<String, Object> reqHeader, Map<String, Object> respHeader) {
//
//    }
//
//}
