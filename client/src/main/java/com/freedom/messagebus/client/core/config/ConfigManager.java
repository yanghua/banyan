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

    private          boolean inited      = false;
    private volatile String  serverState = Constants.MESSAGEBUS_SERVER_EVENT_STOPPED;

    //region handle models
    private List<HandlerModel> produceHandlerModels   = new ArrayList<>();
    private List<HandlerModel> consumeHandlerModels   = new ArrayList<>();
    private List<HandlerModel> requestHandlerModels   = new ArrayList<>();
    private List<HandlerModel> responseHandlerModels  = new ArrayList<>();
    private List<HandlerModel> publishHandlerModels   = new ArrayList<>();
    private List<HandlerModel> subscribeHandlerModels = new ArrayList<>();
    private List<HandlerModel> broadcastHandlerModels = new ArrayList<>();
    //endregion

    //region handler instance
    private List<AbstractHandler> produceHandlerChain   = new ArrayList<>();
    private List<AbstractHandler> consumeHandlerChain   = new ArrayList<>();
    private List<AbstractHandler> requestHandlerChain   = new ArrayList<>();
    private List<AbstractHandler> responseHandlerChain  = new ArrayList<>();
    private List<AbstractHandler> publishHandlerChain   = new ArrayList<>();
    private List<AbstractHandler> subscribeHandlerChain = new ArrayList<>();
    private List<AbstractHandler> broadcastHandlerChain = new ArrayList<>();
    //endregion

    private Map<String, Node>   exchangeNodeMap;
    private Map<String, Node>   queueNodeMap;
    private Map<String, Node>   pubsubNodeMap;
    private Map<String, Node>   appIdQueueMap;
    private Map<String, Config> clientConfigMap;
    private ExchangerManager    exchangeManager;
    private Map<String, String> sendPermissionMap;
    private Map<String, String> receivePermissionMap;
    private Map<String, byte[]> sendPermByteQueryArrMap;
    private Map<String, byte[]> receivePermByteQueryArrMap;

    public ConfigManager() {
        this.inited = this.init();
    }

    private boolean init() {
        try {
            //parse
            parseHandlers("produce", this.produceHandlerModels);
            parseHandlers("consumer", this.consumeHandlerModels);
            parseHandlers("request", this.requestHandlerModels);
            parseHandlers("response", this.responseHandlerModels);
            parseHandlers("publish", this.publishHandlerModels);
            parseHandlers("subscribe", this.subscribeHandlerModels);
            parseHandlers("broadcast", this.broadcastHandlerModels);

            //box
            initHandlers(this.produceHandlerModels, this.produceHandlerChain);
            initHandlers(this.consumeHandlerModels, this.consumeHandlerChain);
            initHandlers(this.requestHandlerModels, this.requestHandlerChain);
            initHandlers(this.responseHandlerModels, this.responseHandlerChain);
            initHandlers(this.publishHandlerModels, this.publishHandlerChain);
            initHandlers(this.subscribeHandlerModels, this.subscribeHandlerChain);
            initHandlers(this.broadcastHandlerModels, this.broadcastHandlerChain);

            if (logger.isDebugEnabled()) {
                printHandlerChain(MessageCarryType.PRODUCE, this.produceHandlerModels);
                printHandlerChain(MessageCarryType.CONSUME, this.consumeHandlerModels);
                printHandlerChain(MessageCarryType.REQUEST, this.requestHandlerModels);
                printHandlerChain(MessageCarryType.RESPONSE, this.responseHandlerModels);
                printHandlerChain(MessageCarryType.PUBLISH, this.publishHandlerModels);
                printHandlerChain(MessageCarryType.SUBSCRIBE, this.subscribeHandlerModels);
                printHandlerChain(MessageCarryType.BROADCAST, this.broadcastHandlerModels);
            }

            return true;
        } catch (Exception e) {
            ExceptionHelper.logException(logger, e, "init");
            return false;
        }
    }

    //region handler model

    public List<HandlerModel> getProduceHandlerModels() {
        return produceHandlerModels;
    }

    public List<HandlerModel> getConsumeHandlerModels() {
        return consumeHandlerModels;
    }

    public List<HandlerModel> getRequestHandlerModels() {
        return requestHandlerModels;
    }

    public List<HandlerModel> getResponseHandlerModels() {
        return responseHandlerModels;
    }

    public List<HandlerModel> getPublishHandlerModels() {
        return publishHandlerModels;
    }

    public List<HandlerModel> getSubscribeHandlerModels() {
        return subscribeHandlerModels;
    }

    public List<HandlerModel> getBroadcastHandlerModels() {
        return broadcastHandlerModels;
    }

    //endregion

    //region handler chain list

    public List<AbstractHandler> getProduceHandlerChain() {
        return produceHandlerChain;
    }

    public List<AbstractHandler> getConsumeHandlerChain() {
        return consumeHandlerChain;
    }

    public List<AbstractHandler> getRequestHandlerChain() {
        return requestHandlerChain;
    }

    public List<AbstractHandler> getResponseHandlerChain() {
        return responseHandlerChain;
    }

    public List<AbstractHandler> getPublishHandlerChain() {
        return publishHandlerChain;
    }

    public List<AbstractHandler> getSubscribeHandlerChain() {
        return subscribeHandlerChain;
    }

    public List<AbstractHandler> getBroadcastHandlerChain() {
        return broadcastHandlerChain;
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
                               List<HandlerModel> handlerModels) {
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
                                          + "/ns:handler");
        xPath.setNamespaceURIs(map);

        List<Element> handlerElements = xPath.selectNodes(doc);

        //iterate each element
        for (Element element : handlerElements) {
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
            handlerModels.add(model);
        }
    }


    private void initHandlers(List<HandlerModel> handlerModels,
                              List<AbstractHandler> handlerChain) {
        try {
            for (HandlerModel model : handlerModels) {
                AbstractHandler handler = (AbstractHandler) Class.forName(model.getHandlerPath()).newInstance();
                handler.init(model);
                handlerChain.add(handler);
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            ExceptionHelper.logException(logger, e, "initHandlers");
            throw new RuntimeException(e);
        }
    }

    private void printHandlerChain(MessageCarryType carryType,
                                   List<HandlerModel> handlerModels) {

        logger.debug("==============" + carryType.toString() + "=============");
        logger.debug("==============handlers=============");
        for (HandlerModel model : handlerModels) {
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
        if (produceHandlerChain != null) {
            for (AbstractHandler handler : produceHandlerChain) {
                handler.destroy();
            }
        }

        if (consumeHandlerChain != null) {
            for (AbstractHandler handler : consumeHandlerChain) {
                handler.destroy();
            }
        }

        if (requestHandlerChain != null) {
            for (AbstractHandler handler : requestHandlerChain) {
                handler.destroy();
            }
        }

        if (responseHandlerChain != null) {
            for (AbstractHandler handler : responseHandlerChain) {
                handler.destroy();
            }
        }

        if (publishHandlerChain != null) {
            for (AbstractHandler handler : publishHandlerChain) {
                handler.destroy();
            }
        }

        if (subscribeHandlerChain != null) {
            for (AbstractHandler handler : subscribeHandlerChain) {
                handler.destroy();
            }
        }

        if (broadcastHandlerChain != null) {
            for (AbstractHandler handler : broadcastHandlerChain) {
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
