package com.freedom.messagebus.client.core.config;

import com.freedom.messagebus.business.exchanger.IExchangerListener;
import com.freedom.messagebus.business.exchanger.ExchangerManager;
import com.freedom.messagebus.business.model.Config;
import com.freedom.messagebus.business.model.Node;
import com.freedom.messagebus.business.model.ReceivePermission;
import com.freedom.messagebus.business.model.SendPermission;
import com.freedom.messagebus.client.core.classLoader.RemoteClassLoader;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.model.HandlerModel;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.CONSTS;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.jetbrains.annotations.NotNull;

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
    private volatile String serverState = CONSTS.MESSAGEBUS_SERVER_EVENT_STOPPED;

    //region handle models
    @NotNull
    private List<HandlerModel> produceHandlerModels;
    @NotNull
    private List<HandlerModel> consumerHandlerModels;
    @NotNull
    private List<HandlerModel> requestHandlerModels;
    @NotNull
    private List<HandlerModel> responseHandlerModels;
    @NotNull
    private List<HandlerModel> publishHandlerModels;
    @NotNull
    private List<HandlerModel> subscribeHandlerModels;
    @NotNull
    private List<HandlerModel> broadcastHandlerModels;
    //endregion

    //region handler instance
    @NotNull
    private List<AbstractHandler> produceHandlerChain;
    @NotNull
    private List<AbstractHandler> consumerHandlerChain;
    @NotNull
    private List<AbstractHandler> requestHandlerChain;
    @NotNull
    private List<AbstractHandler> responseHandlerChain;
    @NotNull
    private List<AbstractHandler> publishHandlerChain;
    @NotNull
    private List<AbstractHandler> subscribeHandlerChain;
    @NotNull
    private List<AbstractHandler> broadcastHandlerChain;
    //endregion

    @NotNull
    private Map<String, Node>   exchangeNodeMap;
    @NotNull
    private Map<String, Node>   queueNodeMap;
    @NotNull
    private Map<String, Node>   pubsubNodeMap;
    @NotNull
    private Map<String, Node>   appIdQueueMap;
    @NotNull
    private Map<String, Config> clientConfigMap;

    private ExchangerManager ZKExchangeManager;

    private Map<String, String> sendPermissionMap;
    private Map<String, String> receivePermissionMap;

    private Map<String, byte[]> sendPermByteQueryArrMap;
    private Map<String, byte[]> receivePermByteQueryArrMap;

    private ConfigManager() {
        this.inited = this.init();
    }

    @NotNull
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
            produceHandlerModels = parseHandlers("produce");
            consumerHandlerModels = parseHandlers("consumer");
            requestHandlerModels = parseHandlers("request");
            responseHandlerModels = parseHandlers("response");
            publishHandlerModels = parseHandlers("publish");
            subscribeHandlerModels = parseHandlers("subscribe");
            broadcastHandlerModels = parseHandlers("broadcast");

            //box
            produceHandlerChain = initHandlers(MessageCarryType.PRODUCE);
            consumerHandlerChain = initHandlers(MessageCarryType.CONSUME);
            requestHandlerChain = initHandlers(MessageCarryType.REQUEST);
            responseHandlerChain = initHandlers(MessageCarryType.RESPONSE);
            publishHandlerChain = initHandlers(MessageCarryType.PUBLISH);
            subscribeHandlerChain = initHandlers(MessageCarryType.SUBSCRIBE);
            broadcastHandlerChain = initHandlers(MessageCarryType.BROADCAST);

            if (logger.isDebugEnabled()) {
                printHandlerChain(MessageCarryType.PRODUCE);
                printHandlerChain(MessageCarryType.CONSUME);
                printHandlerChain(MessageCarryType.REQUEST);
                printHandlerChain(MessageCarryType.RESPONSE);
                printHandlerChain(MessageCarryType.PUBLISH);
                printHandlerChain(MessageCarryType.SUBSCRIBE);
                printHandlerChain(MessageCarryType.BROADCAST);
            }

            return true;
        } catch (Exception e) {
            logger.error("[init] occurs a Exception : " + e.getMessage());
            return false;
        }
    }

    //region handler model
    @NotNull
    public List<HandlerModel> getProduceHandlerModels() {
        return produceHandlerModels;
    }

    @NotNull
    public List<HandlerModel> getConsumerHandlerModels() {
        return consumerHandlerModels;
    }

    @NotNull
    public List<HandlerModel> getRequestHandlerModels() {
        return requestHandlerModels;
    }

    @NotNull
    public List<HandlerModel> getResponseHandlerModels() {
        return responseHandlerModels;
    }

    @NotNull
    public List<HandlerModel> getPublishHandlerModels() {
        return publishHandlerModels;
    }

    @NotNull
    public List<HandlerModel> getSubscribeHandlerModels() {
        return subscribeHandlerModels;
    }

    @NotNull
    public List<HandlerModel> getBroadcastHandlerModels() {
        return broadcastHandlerModels;
    }
    //endregion

    //region handler chain list
    @NotNull
    public List<AbstractHandler> getProduceHandlerChain() {
        return produceHandlerChain;
    }

    @NotNull
    public List<AbstractHandler> getConsumerHandlerChain() {
        return consumerHandlerChain;
    }

    @NotNull
    public List<AbstractHandler> getRequestHandlerChain() {
        return requestHandlerChain;
    }

    @NotNull
    public List<AbstractHandler> getResponseHandlerChain() {
        return responseHandlerChain;
    }

    @NotNull
    public List<AbstractHandler> getPublishHandlerChain() {
        return publishHandlerChain;
    }

    @NotNull
    public List<AbstractHandler> getSubscribeHandlerChain() {
        return subscribeHandlerChain;
    }

    @NotNull
    public List<AbstractHandler> getBroadcastHandlerChain() {
        return broadcastHandlerChain;
    }
    //endregion

    //region node map
    @NotNull
    public Map<String, Node> getExchangeNodeMap() {
        return exchangeNodeMap;
    }

    @NotNull
    public Map<String, Node> getQueueNodeMap() {
        return queueNodeMap;
    }

    @NotNull
    public Map<String, Node> getPubsubNodeMap() {
        return pubsubNodeMap;
    }

    @NotNull
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

    @NotNull
    public Map<String, Config> getClientConfigMap() {
        return clientConfigMap;
    }

    public ExchangerManager getZKExchangeManager() {
        return ZKExchangeManager;
    }

    public void setZKExchangeManager(ExchangerManager ZKExchangeManager) {
        this.ZKExchangeManager = ZKExchangeManager;
    }

    @Deprecated
    public void updateHandlerChain(String path, byte[] data) {
        //TODO:test
        String binaryname = "com.freedom.messagebus.client.handler.produce.MessageSizeValidator";
        RemoteClassLoader rcl = new RemoteClassLoader(data);
        AbstractHandler remoteHandler = null;
        try {
            Class clazz = rcl.loadClass(binaryname);
            remoteHandler = (AbstractHandler) clazz.newInstance();

            //add new handler
            this.produceHandlerChain.add(2, remoteHandler);
        } catch (ClassNotFoundException e) {
            logger.error("[updateHandlerChain] occurs a ClassNotFoundException : " + e.getMessage());
        } catch (InstantiationException e) {
            logger.error("[updateHandlerChain] occurs a InstantiationException : " + e.getMessage());
        } catch (IllegalAccessException e) {
            logger.error("[updateHandlerChain] occurs a IllegalAccessException : " + e.getMessage());
        }

    }

    public synchronized String getServerState() {
        return serverState;
    }

    public synchronized void setServerState(String serverState) {
        this.serverState = serverState;
    }

    @NotNull
    private List<HandlerModel> parseHandlers(String pOrcIdentifier) {
        SAXReader reader = new SAXReader();
        URL url = ConfigManager.class.getClassLoader().getResource("handler.xml");
        Document doc = null;
        List<HandlerModel> result = new ArrayList<HandlerModel>();

        try {
            doc = reader.read(url);
        } catch (DocumentException e) {
            logger.error("[parseHandlers] occurs a DocumentException exception");
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("ns", "http://com.freedom.messagebus");
        XPath xPath = doc.createXPath("//ns:handler-plugins/ns:" + pOrcIdentifier + "/ns:handler");
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
            result.add(model);
        }

        return result;
    }

    @NotNull
    private List<AbstractHandler> initHandlers(MessageCarryType pcEnum) {
        List<AbstractHandler> handlerChain = new ArrayList<AbstractHandler>();
        List<HandlerModel> models = null;

        switch (pcEnum) {
            case PRODUCE:
                models = produceHandlerModels;
                break;

            case CONSUME:
                models = consumerHandlerModels;
                break;

            case REQUEST:
                models = requestHandlerModels;
                break;

            case RESPONSE:
                models = responseHandlerModels;
                break;

            case PUBLISH:
                models = publishHandlerModels;
                break;

            case SUBSCRIBE:
                models = subscribeHandlerModels;
                break;

            case BROADCAST:
                models = broadcastHandlerModels;
                break;

            default: {
                logger.error("[initHandlers] : unknow message handle type");
                models = new ArrayList<HandlerModel>(0);
            }
        }

        try {
            for (HandlerModel model : models) {
                AbstractHandler handler = (AbstractHandler) Class.forName(model.getHandlerPath()).newInstance();
                handler.init(model);
                handlerChain.add(handler);
            }
        } catch (InstantiationException e) {
            logger.error("[initHandlers] : occurs a InstantiationException. message : " + e.getMessage());
        } catch (IllegalAccessException e) {
            logger.error("[initHandlers] : occurs a IllegalAccessException. message : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            logger.error("[initHandlers] : occurs a ClassNotFoundException. message : " + e.getMessage());
        }

        return handlerChain;
    }

    private void printHandlerChain(MessageCarryType pcEnum) {
        List<HandlerModel> handlerModels = null;
        String title = "";

        switch (pcEnum) {
            case PRODUCE: {
                handlerModels = produceHandlerModels;
                title = "produce";
            }
            break;

            case CONSUME: {
                handlerModels = consumerHandlerModels;
                title = "consume";
            }
            break;

            case REQUEST: {
                handlerModels = requestHandlerModels;
                title = "request";
            }
            break;

            case RESPONSE: {
                handlerModels = responseHandlerModels;
                title = "response";
            }
            break;

            case PUBLISH: {
                handlerModels = publishHandlerModels;
                title = "publish";
            }
            break;

            case SUBSCRIBE: {
                handlerModels = subscribeHandlerModels;
                title = "subscribe";
            }
            break;

            case BROADCAST: {
                handlerModels = broadcastHandlerModels;
                title = "broadcast";
            }
            break;

            default: {
                logger.error("unknown message handle type");
                handlerModels = new ArrayList<HandlerModel>(0);
            }
        }

        logger.debug("==============" + title + "=============");
        for (HandlerModel model : handlerModels) {
            logger.debug("              " + model.getHandlerName() + "              ");
            logger.debug("                     ||                     ");
            logger.debug("                     \\/                     ");
        }
    }

    @Override
    public void onZKPathChanged(String path, Object obj) {
        logger.debug("** [onZKPathChanged] ** received change from path : " + path);
        switch (path) {
            case CONSTS.ZOOKEEPER_ROOT_PATH_FOR_ROUTER: {
                this.extractDifferentNodes((List<Node>) obj);
            }
            break;

            case CONSTS.ZOOKEEPER_ROOT_PATH_FOR_CONFIG: {
                this.extractClientConfigs((List<Config>) obj);
            }
            break;

            case CONSTS.ZOOKEEPER_ROOT_PATH_FOR_EVENT: {
                this.setServerState(obj.toString());
            }
            break;

            case CONSTS.ZOOKEEPER_PATH_FOR_AUTH_SEND_PERMISSION: {
                this.processSendPermission((List<SendPermission>) obj);
            }
            break;

            case CONSTS.ZOOKEEPER_PATH_FOR_AUTH_RECEIVE_PERMISSION: {
                this.processReceivePermission((List<ReceivePermission>) obj);
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

        if (consumerHandlerChain != null) {
            for (AbstractHandler handler : consumerHandlerChain) {
                handler.destroy();
            }
        }

    }

    public synchronized void parseZKData() throws IOException {
        this.parseRouterInfo();
        this.parseConfigInfo();
        this.parseSendPermission();
        this.parseReceivePermission();
        //parse event
        String serverState = this.getZKExchangeManager().
            downloadWithPath(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_EVENT).toString();
        this.setServerState(serverState);
    }

    public synchronized void parseRouterInfo() {
//        SAXReader reader = new SAXReader();
//        File routerFile = new File(CONSTS.EXPORTED_NODE_FILE_PATH);
//        URL url = routerFile.toURI().toURL();
//        Document doc = null;
//
//        try {
//            doc = reader.read(url);
//        } catch (DocumentException e) {
//            logger.error("[parseRouterInfo] occurs a DocumentException exception : " + e.getMessage());
//        }
//
//        Element rootElement = doc.getRootElement();
//        org.dom4j.Node databaseNode = rootElement.selectSingleNode("./database");
//
//        List<Element> rowElements = databaseNode.selectNodes("//row");
//        List<Node> nodes = new ArrayList<>(rowElements.size());
//        for (Element row : rowElements) {
//            Node anode = new Node();
//
//            anode.setNodeId(Integer.valueOf(row.selectSingleNode("field[@name='nodeId']").getStringValue()));
//            anode.setName(row.selectSingleNode("field[@name='name']").getStringValue());
//            anode.setValue(row.selectSingleNode("field[@name='value']").getStringValue());
//            anode.setParentId(Integer.valueOf(row.selectSingleNode("field[@name='parentId']").getStringValue()));
//            anode.setType(Short.valueOf(row.selectSingleNode("field[@name='type']").getStringValue()));
//            anode.setLevel(Short.valueOf(row.selectSingleNode("field[@name='level']").getStringValue()));
//            anode.setRouterType(row.selectSingleNode("field[@name='routerType']").getStringValue());
//            anode.setRoutingKey(row.selectSingleNode("field[@name='routingKey']").getStringValue());
//            anode.setAppId(row.selectSingleNode("field[@name='appId']").getStringValue());
//            anode.setAvailable(row.selectSingleNode("field[@name='available']").getStringValue().equals("1"));
//            anode.setInner(row.selectSingleNode("field[@name='inner']").getStringValue().equals("1"));
//
//            nodes.add(anode);
//        }

        if (this.getZKExchangeManager() == null) {
            throw new NullPointerException(" the field exchangeManager can not be null.");
        }

        try {
            List<Node> nodes = (List<Node>) this.getZKExchangeManager().
                downloadWithPath(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_ROUTER);
            this.extractDifferentNodes(nodes);
        } catch (IOException e) {
            logger.error("[parseRouterInfo] occurs a IOException : " + e.getMessage());
        }
    }

    public synchronized void parseConfigInfo() throws MalformedURLException {
//        SAXReader reader = new SAXReader();
//        File routerFile = new File(CONSTS.EXPORTED_CONFIG_FILE_PATH);
//        URL url = routerFile.toURI().toURL();
//        Document doc = null;
//
//        try {
//            doc = reader.read(url);
//        } catch (DocumentException e) {
//            logger.error("[parseConfigInfo] occurs a DocumentException exception : " + e.getMessage());
//        }
//
//        Element rootElement = doc.getRootElement();
//        org.dom4j.Node databaseNode = rootElement.selectSingleNode("./database");
//
//        List<Element> rowElements = databaseNode.selectNodes("//row");
//        List<Config> configItems = new ArrayList<>(rowElements.size());
//        for (Element row : rowElements) {
//            Config config = new Config();
//
//            config.setKey(row.selectSingleNode("field[@name='key']").getStringValue());
//            config.setValue(row.selectSingleNode("field[@name='value']").getStringValue());
//
//            configItems.add(config);
//        }

        if (this.getZKExchangeManager() == null) {
            throw new NullPointerException(" the field exchangeManager can not be null.");
        }

        try {
            List<Config> configs = (List<Config>) this.getZKExchangeManager()
                                                      .downloadWithPath(CONSTS.ZOOKEEPER_ROOT_PATH_FOR_CONFIG);
            this.extractClientConfigs(configs);
        } catch (IOException e) {
            logger.error("[parseConfigInfo] occurs a IOException : " + e.getMessage());
        }

    }

    public synchronized void parseSendPermission() throws MalformedURLException {
//        SAXReader reader = new SAXReader();
//        File routerFile = new File(CONSTS.EXPORTED_SEND_PERMISSION_FILE_PATH);
//        URL url = routerFile.toURI().toURL();
//        Document doc = null;
//
//        try {
//            doc = reader.read(url);
//        } catch (DocumentException e) {
//            logger.error("[parseConfigInfo] occurs a DocumentException exception : " + e.getMessage());
//        }
//
//        Element rootElement = doc.getRootElement();
//        org.dom4j.Node databaseNode = rootElement.selectSingleNode("./database");
//
//        List<Element> rowElements = databaseNode.selectNodes("//row");
//        sendPermissionMap = new ConcurrentHashMap<>(rowElements.size());
//
//        int maxSendPermGrantId = 0;
//
//        for (Element row : rowElements) {
//            String targetId = row.selectSingleNode("field[@name='targetId']").getStringValue();
//            if (!sendPermissionMap.containsKey(targetId)) {
//                sendPermissionMap.put(targetId, "");
//            }
//
//            String joinedGrantIds = sendPermissionMap.get(targetId);
//            String grantId = row.selectSingleNode("field[@name='grantId']").getStringValue();
//            sendPermissionMap.put(targetId, joinedGrantIds + grantId + ",");
//
//            //get max send-permission grant id
//            maxSendPermGrantId = Math.max(maxSendPermGrantId, Integer.valueOf(grantId));
//        }
//
//        sendPermByteQueryArrMap = new ConcurrentHashMap<>(sendPermissionMap.size());
//        for (Map.Entry<String, String> sendPermItem : this.sendPermissionMap.entrySet()) {
//            sendPermByteQueryArrMap.put(sendPermItem.getKey(),
//                                        this.buildQueryArray(maxSendPermGrantId,
//                                                             sendPermItem.getKey(),
//                                                             this.sendPermissionMap)
//                                       );
//        }

        try {
            List<SendPermission> sendPermissions = (List<SendPermission>) this.getZKExchangeManager().downloadWithPath(
                CONSTS.ZOOKEEPER_PATH_FOR_AUTH_SEND_PERMISSION);
            this.processSendPermission(sendPermissions);
        } catch (IOException e) {
            logger.error("[parseSendPermission] occurs a IOException : " + e.getMessage());
        }
    }

    public synchronized void parseReceivePermission() throws MalformedURLException {
//        SAXReader reader = new SAXReader();
//        File routerFile = new File(CONSTS.EXPORTED_RECEIVE_PERMISSION_FILE_PATH);
//        URL url = routerFile.toURI().toURL();
//        Document doc = null;
//
//        try {
//            doc = reader.read(url);
//        } catch (DocumentException e) {
//            logger.error("[parseConfigInfo] occurs a DocumentException exception : " + e.getMessage());
//        }
//
//        Element rootElement = doc.getRootElement();
//        org.dom4j.Node databaseNode = rootElement.selectSingleNode("./database");
//
//        List<Element> rowElements = databaseNode.selectNodes("//row");
//        receivePermissionMap = new ConcurrentHashMap<>(rowElements.size());
//
//        int maxReceivePermGrantId = 0;
//
//        for (Element row : rowElements) {
//            String targetId = row.selectSingleNode("field[@name='targetId']").getStringValue();
//            if (!receivePermissionMap.containsKey(targetId)) {
//                receivePermissionMap.put(targetId, "");
//            }
//
//            String joinedGrantIds = receivePermissionMap.get(targetId);
//            String grantId = row.selectSingleNode("field[@name='grantId']").getStringValue();
//            receivePermissionMap.put(targetId, joinedGrantIds + grantId + ",");
//
//            //get max receive-permission grant id
//            maxReceivePermGrantId = Math.max(maxReceivePermGrantId, Integer.valueOf(grantId));
//        }
//
//        receivePermByteQueryArrMap = new ConcurrentHashMap<>(receivePermissionMap.size());
//        for (Map.Entry<String, String> receivePermItem : receivePermissionMap.entrySet()) {
//            receivePermByteQueryArrMap.put(receivePermItem.getKey(),
//                                           this.buildQueryArray(maxReceivePermGrantId,
//                                                                receivePermItem.getKey(),
//                                                                this.receivePermissionMap)
//                                          );
//        }

        try {
            List<ReceivePermission> receivePermissions = (List<ReceivePermission>) this.getZKExchangeManager().
                downloadWithPath(CONSTS.ZOOKEEPER_PATH_FOR_AUTH_RECEIVE_PERMISSION);

            this.processReceivePermission(receivePermissions);
        } catch (IOException e) {
            logger.error("[parseReceivePermission] occurs a IOException : " + e.getMessage());
        }

    }

    private void extractDifferentNodes(List<Node> nodes) {
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

    private void extractClientConfigs(List<Config> configs) {
        this.clientConfigMap = new ConcurrentHashMap<>();

        for (Config config : configs) {
            if (config.getKey().contains("client"))
                this.clientConfigMap.put(config.getKey(), config);
        }
    }

    private void processSendPermission(List<SendPermission> sendPermissions) {
        sendPermissionMap = new ConcurrentHashMap<>(sendPermissions.size());

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
            maxSendPermGrantId = Math.max(maxSendPermGrantId, Integer.valueOf(grantId));
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

    private void processReceivePermission(List<ReceivePermission> receivePermissions) {
        receivePermissionMap = new ConcurrentHashMap<>(receivePermissions.size());

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
            maxReceivePermGrantId = Math.max(maxReceivePermGrantId, Integer.valueOf(grantId));
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
                int current = Integer.valueOf(grantIds[j]);
                if (i == current) {
                    permissionQueryBytes[i] = 1;
                    break;
                }
            }
        }

        return permissionQueryBytes;
    }
}
