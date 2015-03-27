package com.messagebus.client.core.config;

import com.messagebus.business.exchanger.ExchangerManager;
import com.messagebus.business.exchanger.IExchangerListener;
import com.messagebus.business.model.Channel;
import com.messagebus.business.model.Config;
import com.messagebus.business.model.Node;
import com.messagebus.business.model.Sink;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.model.HandlerModel;
import com.messagebus.client.model.MessageCarryType;
import com.messagebus.common.Constants;
import com.messagebus.common.ExceptionHelper;
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

    private Map<String, Node>    proconNodeMap;
    private Map<String, Node>    reqrespNodeMap;
    private Map<String, Node>    pubsubNodeMap;
    private Map<String, Node>    notificationNodeMap;
    private Map<String, Node>    idNodeMap;
    private Map<String, Node>    secretNodeMap;
    private Map<String, Config>  clientConfigMap;
    private ExchangerManager     exchangeManager;
    private Map<String, Sink>    tokenSinkMap;
    private Map<String, String> pubsubChannelMap;
    private Node                 notificationExchangeNode;

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
    public Map<String, Node> getProconNodeMap() {
        return proconNodeMap;
    }

    public Map<String, Node> getPubsubNodeMap() {
        return pubsubNodeMap;
    }

    public Map<String, Node> getReqrespNodeMap() {
        return reqrespNodeMap;
    }

    public Map<String, Node> getSecretNodeMap() {
        return secretNodeMap;
    }

    public Map<String, Node> getIdNodeMap() {
        return idNodeMap;
    }

    public Map<String, Node> getNotificationNodeMap() {
        return notificationNodeMap;
    }

    //endregion

    public Map<String, Sink> getTokenSinkMap() {
        return tokenSinkMap;
    }

    public Map<String, String> getPubsubChannelMap() {
        return pubsubChannelMap;
    }

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

    public Node getNotificationExchangeNode() {
        return notificationExchangeNode;
    }

    public void setNotificationExchangeNode(Node notificationExchangeNode) {
        this.notificationExchangeNode = notificationExchangeNode;
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
        map.put("ns", "http://com.messagebus");
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

            case Constants.PUBSUB_SINK_CHANNEL: {
                this.processSink((Sink[]) obj);
            }
            break;

            case Constants.PUBSUB_CHANNEL_CHANNEL: {
                this.processChannel((Channel[]) obj);
            }
            break;
        }
    }

    public synchronized void destroy() {
        for (AbstractHandler handler : produceHandlerChain) {
            handler.destroy();
        }

        for (AbstractHandler handler : consumeHandlerChain) {
            handler.destroy();
        }

        for (AbstractHandler handler : requestHandlerChain) {
            handler.destroy();
        }

        for (AbstractHandler handler : responseHandlerChain) {
            handler.destroy();
        }

        for (AbstractHandler handler : publishHandlerChain) {
            handler.destroy();
        }

        for (AbstractHandler handler : subscribeHandlerChain) {
            handler.destroy();
        }

        for (AbstractHandler handler : broadcastHandlerChain) {
            handler.destroy();
        }

    }

    public synchronized void parseRealTimeData() throws IOException {
        this.parseRouterInfo();
        this.parseConfigInfo();
        this.parseSink();
        this.parseChannel();
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

        Node[] nodes = (Node[]) this.getExchangeManager().
            downloadWithChannel(Constants.PUBSUB_ROUTER_CHANNEL);
        this.extractDifferentNodes(nodes);
    }

    public synchronized void parseConfigInfo() throws MalformedURLException {
        if (this.getExchangeManager() == null) {
            throw new NullPointerException(" the field exchangeManager can not be null.");
        }

        Config[] configs = (Config[]) this.getExchangeManager()
                                          .downloadWithChannel(Constants.PUBSUB_CONFIG_CHANNEL);
        this.extractClientConfigs(configs);
    }

    public synchronized void parseSink() {
        Sink[] sinks = (Sink[]) this.getExchangeManager().downloadWithChannel(Constants.PUBSUB_SINK_CHANNEL);
        this.processSink(sinks);
    }

    public synchronized void parseChannel() {
        Channel[] channels = (Channel[]) this.getExchangeManager().downloadWithChannel(Constants.PUBSUB_CHANNEL_CHANNEL);
        this.processChannel(channels);
    }

    private void extractDifferentNodes(Node[] nodes) {
        this.secretNodeMap = new ConcurrentHashMap<>();
        this.proconNodeMap = new ConcurrentHashMap<>();
        this.pubsubNodeMap = new ConcurrentHashMap<>();
        this.notificationNodeMap = new ConcurrentHashMap<>();
        this.reqrespNodeMap = new ConcurrentHashMap<>();
        this.idNodeMap = new ConcurrentHashMap<>(nodes.length);

        for (Node node : nodes) {
            idNodeMap.put(node.getNodeId(), node);

            if (node.getType().equals("0") && node.getName().equals(Constants.NOTIFICATION_EXCHANGE_NAME)) {
                this.notificationExchangeNode = node;
            }

            if (node.getType().equals("1") || !node.isInner()) {
                this.secretNodeMap.put(node.getSecret(), node);
                if (node.getValue().contains("procon")) {
                    this.proconNodeMap.put(node.getName(), node);
                } else if (node.getValue().contains("reqresp")) {
                    this.reqrespNodeMap.put(node.getName(), node);
                } else if (node.getValue().contains("pubsub")) {
                    this.pubsubNodeMap.put(node.getName(), node);
                } else if (node.getValue().contains("notification")) {
                    this.notificationNodeMap.put(node.getName(), node);
                }
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

    private void processSink(Sink[] sinks) {
        tokenSinkMap = new ConcurrentHashMap<>(sinks.length);

        for (Sink sink : sinks) {
            tokenSinkMap.put(sink.getToken(), sink);
        }
    }

    private void processChannel(Channel[] channels) {
        pubsubChannelMap = new ConcurrentHashMap<>();

        for (Channel channel : channels) {
            if (pubsubChannelMap.containsKey(channel.getPushFrom())) {
                String tmp = pubsubChannelMap.get(channel.getPushFrom());
                tmp += ("," + channel.getPushTo());

                pubsubChannelMap.put(channel.getPushFrom(), tmp);
            } else {
                pubsubChannelMap.put(channel.getPushFrom(), channel.getPushTo());
            }
        }
    }

}
