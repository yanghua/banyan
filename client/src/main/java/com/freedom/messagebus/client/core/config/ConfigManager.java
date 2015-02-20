package com.freedom.messagebus.client.core.config;

import com.freedom.messagebus.business.exchanger.ExchangerManager;
import com.freedom.messagebus.business.exchanger.IExchangerListener;
import com.freedom.messagebus.business.model.Config;
import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.business.model.ReceivePermission;
import com.freedom.messagebus.business.model.SendPermission;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.model.HandlerModel;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.Constants;
import com.freedom.messagebus.common.ExceptionHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * the config manager
 */
public class ConfigManager implements IExchangerListener {

    private static final Log logger = LogFactory.getLog(ConfigManager.class);

    private boolean inited = false;
    private static volatile ConfigManager instance;
    private volatile String serverState = Constants.MESSAGEBUS_SERVER_EVENT_STOPPED;

    //region handle models
    private List<HandlerModel> preProduceHandlerModels  = new ArrayList<>();
    private List<HandlerModel> postProduceHandlerModels = new ArrayList<>();

    private List<HandlerModel> preConsumeHandlerModels  = new ArrayList<>();
    private List<HandlerModel> postConsumeHandlerModels = new ArrayList<>();

    private List<HandlerModel> preRequestHandlerModels  = new ArrayList<>();
    private List<HandlerModel> postRequestHandlerModels = new ArrayList<>();

    private List<HandlerModel> preResponseHandlerModels  = new ArrayList<>();
    private List<HandlerModel> postResponseHandlerModels = new ArrayList<>();

    private List<HandlerModel> prePublishHandlerModels  = new ArrayList<>();
    private List<HandlerModel> postPublishHandlerModels = new ArrayList<>();

    private List<HandlerModel> preSubscribeHandlerModels  = new ArrayList<>();
    private List<HandlerModel> postSubscribeHandlerModels = new ArrayList<>();

    private List<HandlerModel> preBroadcastHandlerModels  = new ArrayList<>();
    private List<HandlerModel> postBroadcastHandlerModels = new ArrayList<>();
    //endregion

    //region handler instance

    private List<AbstractHandler> preProduceHandlerChain  = new ArrayList<>();
    private List<AbstractHandler> postProduceHandlerChain = new ArrayList<>();

    private List<AbstractHandler> preConsumeHandlerChain  = new ArrayList<>();
    private List<AbstractHandler> postConsumeHandlerChain = new ArrayList<>();

    private List<AbstractHandler> preRequestHandlerChain  = new ArrayList<>();
    private List<AbstractHandler> postRequestHandlerChain = new ArrayList<>();

    private List<AbstractHandler> preResponseHandlerChain  = new ArrayList<>();
    private List<AbstractHandler> postResponseHandlerChain = new ArrayList<>();

    private List<AbstractHandler> prePublishHandlerChain  = new ArrayList<>();
    private List<AbstractHandler> postPublishHandlerChain = new ArrayList<>();

    private List<AbstractHandler> preSubscribeHandlerChain  = new ArrayList<>();
    private List<AbstractHandler> postSubscribeHandlerChain = new ArrayList<>();

    private List<AbstractHandler> preBroadcastHandlerChain  = new ArrayList<>();
    private List<AbstractHandler> postBroadcastHandlerChain = new ArrayList<>();
    //endregion


    private Map<String, Node> exchangeNodeMap;

    private Map<String, Node> queueNodeMap;

    private Map<String, Node> pubsubNodeMap;

    private Map<String, Node> appIdQueueMap;

    private Map<String, Config> clientConfigMap;

    private ExchangerManager exchangeManager;

    private Map<String, String> sendPermissionMap;
    private Map<String, String> receivePermissionMap;

    private Map<String, byte[]> sendPermByteQueryArrMap;
    private Map<String, byte[]> receivePermByteQueryArrMap;

    private ConfigManager() {
        this.inited = this.init();
    }


    public static ConfigManager getInstance() {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager();
                }
            }
        }

        return instance;
    }

    private boolean init() {
        try {
            //parse
            parseHandlers("produce", this.preProduceHandlerModels,
                          this.postProduceHandlerModels);
            parseHandlers("consumer", this.preConsumeHandlerModels,
                          this.postConsumeHandlerModels);
            parseHandlers("request", this.preRequestHandlerModels,
                          this.postRequestHandlerModels);
            parseHandlers("response", this.preResponseHandlerModels,
                          this.postResponseHandlerModels);
            parseHandlers("publish", this.prePublishHandlerModels,
                          this.postPublishHandlerModels);
            parseHandlers("subscribe", this.preSubscribeHandlerModels,
                          this.postSubscribeHandlerModels);
            parseHandlers("broadcast", this.preBroadcastHandlerModels,
                          this.postBroadcastHandlerModels);

            //box
            initHandlers(this.preProduceHandlerModels,
                         this.postProduceHandlerModels,
                         this.preProduceHandlerChain,
                         this.postProduceHandlerChain);
            initHandlers(this.preConsumeHandlerModels,
                         this.postConsumeHandlerModels,
                         this.preConsumeHandlerChain,
                         this.postConsumeHandlerChain);
            initHandlers(this.preRequestHandlerModels,
                         this.postRequestHandlerModels,
                         this.preRequestHandlerChain,
                         this.postRequestHandlerChain);
            initHandlers(this.preResponseHandlerModels,
                         this.postResponseHandlerModels,
                         this.preResponseHandlerChain,
                         this.postResponseHandlerChain);
            initHandlers(this.prePublishHandlerModels,
                         this.postPublishHandlerModels,
                         this.prePublishHandlerChain,
                         this.postPublishHandlerChain);
            initHandlers(this.preSubscribeHandlerModels,
                         this.postSubscribeHandlerModels,
                         this.preSubscribeHandlerChain,
                         this.postSubscribeHandlerChain);
            initHandlers(this.preBroadcastHandlerModels,
                         this.postBroadcastHandlerModels,
                         this.preBroadcastHandlerChain,
                         this.postBroadcastHandlerChain);

            if (logger.isDebugEnabled()) {
                printHandlerChain(MessageCarryType.PRODUCE,
                                  this.preProduceHandlerModels,
                                  this.postProduceHandlerModels);
                printHandlerChain(MessageCarryType.CONSUME,
                                  this.preConsumeHandlerModels,
                                  this.postConsumeHandlerModels);
                printHandlerChain(MessageCarryType.REQUEST,
                                  this.preRequestHandlerModels,
                                  this.postRequestHandlerModels);
                printHandlerChain(MessageCarryType.RESPONSE,
                                  this.preResponseHandlerModels,
                                  this.postResponseHandlerModels);
                printHandlerChain(MessageCarryType.PUBLISH,
                                  this.prePublishHandlerModels,
                                  this.postPublishHandlerModels);
                printHandlerChain(MessageCarryType.SUBSCRIBE,
                                  this.preSubscribeHandlerModels,
                                  this.postSubscribeHandlerModels);
                printHandlerChain(MessageCarryType.BROADCAST,
                                  this.preBroadcastHandlerModels,
                                  this.postBroadcastHandlerModels);
            }

            return true;
        } catch (Exception e) {
            logger.error("[init] occurs a Exception : " + e.getMessage());
            return false;
        }
    }

    //region handler model

    public List<HandlerModel> getPreProduceHandlerModels() {
        return preProduceHandlerModels;
    }

    public List<HandlerModel> getPreConsumeHandlerModels() {
        return preConsumeHandlerModels;
    }

    public List<HandlerModel> getPreRequestHandlerModels() {
        return preRequestHandlerModels;
    }

    public List<HandlerModel> getPreResponseHandlerModels() {
        return preResponseHandlerModels;
    }

    public List<HandlerModel> getPrePublishHandlerModels() {
        return prePublishHandlerModels;
    }

    public List<HandlerModel> getPreSubscribeHandlerModels() {
        return preSubscribeHandlerModels;
    }

    public List<HandlerModel> getPreBroadcastHandlerModels() {
        return preBroadcastHandlerModels;
    }

    public List<HandlerModel> getPostProduceHandlerModels() {
        return postProduceHandlerModels;
    }

    public List<HandlerModel> getPostConsumeHandlerModels() {
        return postConsumeHandlerModels;
    }

    public List<HandlerModel> getPostRequestHandlerModels() {
        return postRequestHandlerModels;
    }

    public List<HandlerModel> getPostResponseHandlerModels() {
        return postResponseHandlerModels;
    }

    public List<HandlerModel> getPostPublishHandlerModels() {
        return postPublishHandlerModels;
    }

    public List<HandlerModel> getPostSubscribeHandlerModels() {
        return postSubscribeHandlerModels;
    }

    public List<HandlerModel> getPostBroadcastHandlerModels() {
        return postBroadcastHandlerModels;
    }

    //endregion

    //region handler chain list

    public List<AbstractHandler> getPreProduceHandlerChain() {
        return preProduceHandlerChain;
    }

    public List<AbstractHandler> getPreConsumeHandlerChain() {
        return preConsumeHandlerChain;
    }

    public List<AbstractHandler> getPreRequestHandlerChain() {
        return preRequestHandlerChain;
    }

    public List<AbstractHandler> getPreResponseHandlerChain() {
        return preResponseHandlerChain;
    }

    public List<AbstractHandler> getPrePublishHandlerChain() {
        return prePublishHandlerChain;
    }

    public List<AbstractHandler> getPreSubscribeHandlerChain() {
        return preSubscribeHandlerChain;
    }

    public List<AbstractHandler> getPreBroadcastHandlerChain() {
        return preBroadcastHandlerChain;
    }

    public List<AbstractHandler> getPostProduceHandlerChain() {
        return postProduceHandlerChain;
    }

    public List<AbstractHandler> getPostConsumeHandlerChain() {
        return postConsumeHandlerChain;
    }

    public List<AbstractHandler> getPostRequestHandlerChain() {
        return postRequestHandlerChain;
    }

    public List<AbstractHandler> getPostResponseHandlerChain() {
        return postResponseHandlerChain;
    }

    public List<AbstractHandler> getPostPublishHandlerChain() {
        return postPublishHandlerChain;
    }

    public List<AbstractHandler> getPostSubscribeHandlerChain() {
        return postSubscribeHandlerChain;
    }

    public List<AbstractHandler> getPostBroadcastHandlerChain() {
        return postBroadcastHandlerChain;
    }

    //endregion

    //region node map

    public Map<String, Node> getExchangeNodeMap() {
        return exchangeNodeMap;
    }


    public Map<String, Node> getQueueNodeMap() {
        return queueNodeMap;
    }


    public Map<String, Node> getPubsubNodeMap() {
        return pubsubNodeMap;
    }


    public Map<String, Node> getAppIdQueueMap() {
        return appIdQueueMap;
    }
    //endregion

    //region permission
    public Map<String, String> getSendPermissionMap() {
        return sendPermissionMap;
    }

    public Map<String, String> getReceivePermissionMap() {
        return receivePermissionMap;
    }

    public Map<String, byte[]> getSendPermByteQueryArrMap() {
        return sendPermByteQueryArrMap;
    }

    public Map<String, byte[]> getReceivePermByteQueryArrMap() {
        return receivePermByteQueryArrMap;
    }
    //endregion


    public Map<String, Config> getClientConfigMap() {
        return clientConfigMap;
    }

    public ExchangerManager getExchangeManager() {
        return exchangeManager;
    }

    public void setExchangeManager(ExchangerManager exchangeManager) {
        this.exchangeManager = exchangeManager;
    }

    public synchronized String getServerState() {
        return serverState;
    }

    public synchronized void setServerState(String serverState) {
        this.serverState = serverState;
    }


    private void parseHandlers(String messageCarryTypeStr,
                               List<HandlerModel> preHandlerModels,
                               List<HandlerModel> postHandlerModels) {
        SAXReader reader = new SAXReader();
        URL url = ConfigManager.class.getClassLoader().getResource("handler.xml");
        Document doc = null;

        try {
            doc = reader.read(url);
        } catch (DocumentException e) {
            ExceptionHelper.logException(logger, e, "parseHandlers");
            throw new RuntimeException(e);
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("ns", "http://com.freedom.messagebus");
        XPath xPath = doc.createXPath("//ns:handler-plugins/ns:" + messageCarryTypeStr
                                          + "/ns:prehandlers" + "/ns:handler");
        xPath.setNamespaceURIs(map);

        List<Element> preHandlerElements = xPath.selectNodes(doc);

        //iterate each element
        for (Element element : preHandlerElements) {
            xPath = element.createXPath("ns:handler-name");
            xPath.setNamespaceURIs(map);

            org.dom4j.Node handlerNameNode = xPath.selectSingleNode(element);
            String handlerName = handlerNameNode.getStringValue();

            xPath = element.createXPath("ns:handler-path");
            xPath.setNamespaceURIs(map);

            org.dom4j.Node handlerPathNode = xPath.selectSingleNode(element);
            String handlerPath = handlerPathNode.getStringValue();

            HandlerModel model = new HandlerModel();
            model.setHandlerName(handlerName);
            model.setHandlerPath(handlerPath);
            preHandlerModels.add(model);
        }

        map.put("ns", "http://com.freedom.messagebus");
        xPath = doc.createXPath("//ns:handler-plugins/ns:" + messageCarryTypeStr
                                    + "/ns:posthandlers" + "/ns:handler");
        xPath.setNamespaceURIs(map);

        List<Element> postHandlerElements = xPath.selectNodes(doc);

        //iterate each element
        for (Element element : postHandlerElements) {
            xPath = element.createXPath("ns:handler-name");
            xPath.setNamespaceURIs(map);

            org.dom4j.Node handlerNameNode = xPath.selectSingleNode(element);
            String handlerName = handlerNameNode.getStringValue();

            xPath = element.createXPath("ns:handler-path");
            xPath.setNamespaceURIs(map);

            org.dom4j.Node handlerPathNode = xPath.selectSingleNode(element);
            String handlerPath = handlerPathNode.getStringValue();

            HandlerModel model = new HandlerModel();
            model.setHandlerName(handlerName);
            model.setHandlerPath(handlerPath);
            postHandlerModels.add(model);
        }

    }


    private void initHandlers(List<HandlerModel> preHandlerModels,
                              List<HandlerModel> postHandlerModels,
                              List<AbstractHandler> preHandlerChain,
                              List<AbstractHandler> postHandlerChain) {
        try {
            for (HandlerModel model : preHandlerModels) {
                AbstractHandler handler = (AbstractHandler) Class.forName(model.getHandlerPath()).newInstance();
                handler.init(model);
                preHandlerChain.add(handler);
            }

            for (HandlerModel model : postHandlerModels) {
                AbstractHandler handler = (AbstractHandler) Class.forName(model.getHandlerPath()).newInstance();
                handler.init(model);
                postHandlerChain.add(handler);
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            ExceptionHelper.logException(logger, e, "initHandlers");
            throw new RuntimeException(e);
        }
    }

    private void printHandlerChain(MessageCarryType carryType,
                                   List<HandlerModel> preHandlerModels,
                                   List<HandlerModel> postHandlerModels) {

        logger.debug("==============" + carryType.toString() + "=============");
        logger.debug("==============prehandlers=============");
        for (HandlerModel model : preHandlerModels) {
            logger.debug("              " + model.getHandlerName() + "              ");
            logger.debug("                     ||                     ");
            logger.debug("                     \\/                     ");
        }

        logger.debug("==============posthandlers=============");
        for (HandlerModel model : postHandlerModels) {
            logger.debug("              " + model.getHandlerName() + "              ");
            logger.debug("                     ||                     ");
            logger.debug("                     \\/                     ");
        }
    }

    @Override
    public void onChannelDataChanged(String channel, Object obj) {
        logger.debug("** [onChannelDataChanged] ** received change from path : " + channel);
        switch (channel) {
            case Constants.PUBSUB_ROUTER_CHANNEL: {
                this.extractDifferentNodes((Node[]) obj);
            }
            break;

            case Constants.PUBSUB_CONFIG_CHANNEL: {
                this.extractClientConfigs((Config[]) obj);
            }
            break;

            case Constants.PUBSUB_EVENT_CHANNEL: {
                logger.debug("received event value : " + obj.toString());
                this.setServerState(obj.toString());
            }
            break;

            case Constants.PUBSUB_AUTH_SEND_PERMISSION_CHANNEL: {
                this.processSendPermission((SendPermission[]) obj);
            }
            break;

            case Constants.PUBSUB_AUTH_RECEIVE_PERMISSION_CHANNEL: {
                this.processReceivePermission((ReceivePermission[]) obj);
            }
            break;
        }
    }

    public synchronized void destroy() {
        if (preProduceHandlerChain != null) {
            for (AbstractHandler handler : preProduceHandlerChain) {
                handler.destroy();
            }
        }

        if (postProduceHandlerChain != null) {
            for (AbstractHandler handler : postProduceHandlerChain) {
                handler.destroy();
            }
        }

        if (preConsumeHandlerChain != null) {
            for (AbstractHandler handler : preConsumeHandlerChain) {
                handler.destroy();
            }
        }

        if (postConsumeHandlerChain != null) {
            for (AbstractHandler handler : postConsumeHandlerChain) {
                handler.destroy();
            }
        }

        if (preRequestHandlerChain != null) {
            for (AbstractHandler handler : preRequestHandlerChain) {
                handler.destroy();
            }
        }

        if (postRequestHandlerChain != null) {
            for (AbstractHandler handler : postRequestHandlerChain) {
                handler.destroy();
            }
        }

        if (preResponseHandlerChain != null) {
            for (AbstractHandler handler : preResponseHandlerChain) {
                handler.destroy();
            }
        }

        if (postResponseHandlerChain != null) {
            for (AbstractHandler handler : postResponseHandlerChain) {
                handler.destroy();
            }
        }

        if (prePublishHandlerChain != null) {
            for (AbstractHandler handler : prePublishHandlerChain) {
                handler.destroy();
            }
        }

        if (postPublishHandlerChain != null) {
            for (AbstractHandler handler : postPublishHandlerChain) {
                handler.destroy();
            }
        }

        if (preSubscribeHandlerChain != null) {
            for (AbstractHandler handler : preSubscribeHandlerChain) {
                handler.destroy();
            }
        }

        if (postSubscribeHandlerChain != null) {
            for (AbstractHandler handler : postSubscribeHandlerChain) {
                handler.destroy();
            }
        }

        if (preBroadcastHandlerChain != null) {
            for (AbstractHandler handler : preBroadcastHandlerChain) {
                handler.destroy();
            }
        }

        if (postBroadcastHandlerChain != null) {
            for (AbstractHandler handler : postBroadcastHandlerChain) {
                handler.destroy();
            }
        }
    }

    public synchronized void parseRealTimeData() throws IOException {
        this.parseRouterInfo();
        this.parseConfigInfo();
        this.parseSendPermission();
        this.parseReceivePermission();
        //parse event
        Object tmp = this.getExchangeManager().downloadWithChannel(Constants.PUBSUB_EVENT_CHANNEL);
        if (tmp != null) {
            String serverState = tmp.toString();
            this.setServerState(serverState);
        }
    }

    public synchronized void parseRouterInfo() {
        if (this.getExchangeManager() == null) {
            throw new NullPointerException(" the field exchangeManager can not be null.");
        }

        try {
            Node[] nodes = (Node[]) this.getExchangeManager().
                downloadWithChannel(Constants.PUBSUB_ROUTER_CHANNEL);
            this.extractDifferentNodes(nodes);
        } catch (IOException e) {
            logger.error("[parseRouterInfo] occurs a IOException : " + e.getMessage());
        }
    }

    public synchronized void parseConfigInfo() throws MalformedURLException {
        if (this.getExchangeManager() == null) {
            throw new NullPointerException(" the field exchangeManager can not be null.");
        }

        try {
            Config[] configs = (Config[]) this.getExchangeManager()
                                              .downloadWithChannel(Constants.PUBSUB_CONFIG_CHANNEL);
            this.extractClientConfigs(configs);
        } catch (IOException e) {
            logger.error("[parseConfigInfo] occurs a IOException : " + e.getMessage());
        }

    }

    public synchronized void parseSendPermission() throws MalformedURLException {
        try {
            SendPermission[] sendPermissions = (SendPermission[]) this.getExchangeManager().downloadWithChannel(
                Constants.PUBSUB_AUTH_SEND_PERMISSION_CHANNEL);
            this.processSendPermission(sendPermissions);
        } catch (IOException e) {
            logger.error("[parseSendPermission] occurs a IOException : " + e.getMessage());
        }
    }

    public synchronized void parseReceivePermission() throws MalformedURLException {
        try {
            ReceivePermission[] receivePermissions = (ReceivePermission[]) this.getExchangeManager().
                downloadWithChannel(Constants.PUBSUB_AUTH_RECEIVE_PERMISSION_CHANNEL);

            this.processReceivePermission(receivePermissions);
        } catch (IOException e) {
            logger.error("[parseReceivePermission] occurs a IOException : " + e.getMessage());
        }

    }

    private void extractDifferentNodes(Node[] nodes) {
        this.exchangeNodeMap = new ConcurrentHashMap<>();
        this.queueNodeMap = new ConcurrentHashMap<>();
        this.pubsubNodeMap = new ConcurrentHashMap<>();
        this.appIdQueueMap = new ConcurrentHashMap<>();

        for (Node node : nodes) {
            if (node.getType() == 0)
                this.exchangeNodeMap.put(node.getName(), node);
            else {
                this.appIdQueueMap.put(node.getAppId(), node);
                if (node.getType() == 1 && !node.getValue().contains("pubsub"))
                    this.queueNodeMap.put(node.getName(), node);
                else if (node.getType() == 1 && node.getValue().contains("pubsub"))
                    this.pubsubNodeMap.put(node.getName(), node);
            }

        }
    }

    private void extractClientConfigs(Config[] configs) {
        this.clientConfigMap = new ConcurrentHashMap<>();

        for (Config config : configs) {
            if (config.getKey().contains("client"))
                this.clientConfigMap.put(config.getKey(), config);
        }
    }

    private void processSendPermission(SendPermission[] sendPermissions) {
        sendPermissionMap = new ConcurrentHashMap<>(sendPermissions.length);

        int maxSendPermGrantId = 0;

        for (SendPermission sendPermission : sendPermissions) {
            String targetId = String.valueOf(sendPermission.getTargetId());
            if (!sendPermissionMap.containsKey(targetId)) {
                sendPermissionMap.put(targetId, "");
            }

            String joinedGrantIds = sendPermissionMap.get(targetId);
            String grantId = String.valueOf(sendPermission.getGrantId());
            sendPermissionMap.put(targetId, joinedGrantIds + grantId + ",");

            //get max send-permission grant id
            maxSendPermGrantId = Math.max(maxSendPermGrantId, Integer.parseInt(grantId));
        }

        sendPermByteQueryArrMap = new ConcurrentHashMap<>(sendPermissionMap.size());
        for (Map.Entry<String, String> sendPermItem : this.sendPermissionMap.entrySet()) {
            sendPermByteQueryArrMap.put(sendPermItem.getKey(),
                                        this.buildQueryArray(maxSendPermGrantId,
                                                             sendPermItem.getKey(),
                                                             this.sendPermissionMap)
                                       );
        }
    }

    private void processReceivePermission(ReceivePermission[] receivePermissions) {
        receivePermissionMap = new ConcurrentHashMap<>(receivePermissions.length);

        int maxReceivePermGrantId = 0;

        for (ReceivePermission receivePermission : receivePermissions) {
            String targetId = String.valueOf(receivePermission.getTargetId());
            if (!receivePermissionMap.containsKey(targetId)) {
                receivePermissionMap.put(targetId, "");
            }

            String joinedGrantIds = receivePermissionMap.get(targetId);
            String grantId = String.valueOf(receivePermission.getGrantId());
            receivePermissionMap.put(targetId, joinedGrantIds + grantId + ",");

            //get max receive-permission grant id
            maxReceivePermGrantId = Math.max(maxReceivePermGrantId, Integer.parseInt(grantId));
        }

        receivePermByteQueryArrMap = new ConcurrentHashMap<>(receivePermissionMap.size());
        for (Map.Entry<String, String> receivePermItem : receivePermissionMap.entrySet()) {
            receivePermByteQueryArrMap.put(receivePermItem.getKey(),
                                           this.buildQueryArray(maxReceivePermGrantId,
                                                                receivePermItem.getKey(),
                                                                this.receivePermissionMap)
                                          );
        }
    }

    private byte[] buildQueryArray(int maxGrantId, String targetIdStr, Map<String, String> map) {
        //from 1 - maxGrantId
        byte[] permissionQueryBytes = new byte[maxGrantId + 1];

        String[] grantIds = map.get(targetIdStr).toString().split(",");

        permissionQueryBytes[0] = 0;
        for (int i = 0; i <= maxGrantId; i++) {
            permissionQueryBytes[i] = 0;

            for (int j = 0; j < grantIds.length; j++) {
                int current = Integer.parseInt(grantIds[j]);
                if (i == current) {
                    permissionQueryBytes[i] = 1;
                    break;
                }
            }
        }

        return permissionQueryBytes;
    }
}
