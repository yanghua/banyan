package com.messagebus.client;

import com.google.common.base.Strings;
import com.messagebus.client.handler.AbstractHandler;
import com.messagebus.client.model.*;
import com.messagebus.common.Constants;
import com.messagebus.common.ExceptionHelper;
import com.messagebus.interactor.pubsub.IPubsuberDataListener;
import com.messagebus.interactor.pubsub.PubsuberManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * the config manager
 */
public class ConfigManager implements IPubsuberDataListener {

    private static final Log logger = LogFactory.getLog(ConfigManager.class);

    private boolean inited = false;

    private volatile String                serverState       = Constants.MESSAGEBUS_SERVER_EVENT_STOPPED;
    private          Map<String, NodeView> secretNodeViewMap = new ConcurrentHashMap<String, NodeView>();
    private          Map<String, String>   configMap         = new ConcurrentHashMap<String, String>();

    //region handle models
    private List<HandlerModel> produceHandlerModels     = new ArrayList<HandlerModel>();
    private List<HandlerModel> consumeHandlerModels     = new ArrayList<HandlerModel>();
    private List<HandlerModel> requestHandlerModels     = new ArrayList<HandlerModel>();
    private List<HandlerModel> responseHandlerModels    = new ArrayList<HandlerModel>();
    private List<HandlerModel> publishHandlerModels     = new ArrayList<HandlerModel>();
    private List<HandlerModel> subscribeHandlerModels   = new ArrayList<HandlerModel>();
    private List<HandlerModel> broadcastHandlerModels   = new ArrayList<HandlerModel>();
    private List<HandlerModel> rpcRequestHandlerModels  = new ArrayList<HandlerModel>();
    private List<HandlerModel> rpcResponseHandlerModels = new ArrayList<HandlerModel>();
    //endregion

    //region handler instance
    private List<AbstractHandler> produceHandlerChain     = new ArrayList<AbstractHandler>();
    private List<AbstractHandler> consumeHandlerChain     = new ArrayList<AbstractHandler>();
    private List<AbstractHandler> requestHandlerChain     = new ArrayList<AbstractHandler>();
    private List<AbstractHandler> responseHandlerChain    = new ArrayList<AbstractHandler>();
    private List<AbstractHandler> publishHandlerChain     = new ArrayList<AbstractHandler>();
    private List<AbstractHandler> subscribeHandlerChain   = new ArrayList<AbstractHandler>();
    private List<AbstractHandler> broadcastHandlerChain   = new ArrayList<AbstractHandler>();
    private List<AbstractHandler> rpcRequestHandlerChain  = new ArrayList<AbstractHandler>();
    private List<AbstractHandler> rpcResponseHandlerChain = new ArrayList<AbstractHandler>();
    //endregion

    private PubsuberManager pubsuberManager;
    private Node            notificationExchangeNode;

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
            parseHandlers("rpcrequest", this.rpcRequestHandlerModels);
            parseHandlers("rpcresponse", this.rpcResponseHandlerModels);

            //box
            initHandlers(this.produceHandlerModels, this.produceHandlerChain);
            initHandlers(this.consumeHandlerModels, this.consumeHandlerChain);
            initHandlers(this.requestHandlerModels, this.requestHandlerChain);
            initHandlers(this.responseHandlerModels, this.responseHandlerChain);
            initHandlers(this.publishHandlerModels, this.publishHandlerChain);
            initHandlers(this.subscribeHandlerModels, this.subscribeHandlerChain);
            initHandlers(this.broadcastHandlerModels, this.broadcastHandlerChain);
            initHandlers(this.rpcRequestHandlerModels, this.rpcRequestHandlerChain);
            initHandlers(this.rpcResponseHandlerModels, this.rpcResponseHandlerChain);

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

    public List<HandlerModel> getRpcRequestHandlerModels() {
        return rpcRequestHandlerModels;
    }

    public List<HandlerModel> getRpcResponseHandlerModels() {
        return rpcResponseHandlerModels;
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

    public List<AbstractHandler> getRpcRequestHandlerChain() {
        return rpcRequestHandlerChain;
    }

    public List<AbstractHandler> getRpcResponseHandlerChain() {
        return rpcResponseHandlerChain;
    }

    //endregion

    public PubsuberManager getPubsuberManager() {
        return pubsuberManager;
    }

    public void setPubsuberManager(PubsuberManager pubsuberManager) {
        this.pubsuberManager = pubsuberManager;
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
        } catch (InstantiationException e) {
            ExceptionHelper.logException(logger, e, "initHandlers");
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            ExceptionHelper.logException(logger, e, "initHandlers");
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
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
        logger.debug("=-=-=-=-=-=- received change from channel : " + channel + " =-=-=-=-=-=-");
        if (channel.equals(Constants.PUBSUB_NODEVIEW_CHANNEL)) {
            String secret = obj.toString();
            this.updateNodeView(secret);
        } else if (channel.equals(Constants.PUBSUB_SERVER_STATE_CHANNEL)) {
            String serverState = obj.toString();
            this.setServerState(serverState);
        } else if (channel.equals(Constants.PUBSUB_CONFIG_CHANNEL)) {
            this.updateConfig(obj.toString());
        } else if (channel.equals(Constants.PUBSUB_NOTIFICATION_EXCHANGE_CHANNEL)) {
            this.updateNotificationNode();
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

    public synchronized void checkServerState() {
        String tmp = this.getPubsuberManager().get(Constants.PUBSUB_SERVER_STATE_CHANNEL, String.class);
        logger.debug("current server state is : " + tmp);
        if (tmp != null) {
            this.setServerState(tmp);
        }
    }

    public synchronized String getConfig(String key) {
        if (this.configMap.containsKey(key)) {
            return this.configMap.get(key);
        } else {
            String configValue = this.pubsuberManager.get(key, String.class);
            this.configMap.put(key, configValue);
            return configValue;
        }
    }

    public synchronized void updateConfig(String key) {
        if (this.configMap.containsKey(key)) {
            this.configMap.remove(key);
            this.getConfig(key);
        }
    }

    public synchronized Node getNotificationExchangeNode() {
        if (this.notificationExchangeNode != null) {
            return this.notificationExchangeNode;
        } else {
            this.notificationExchangeNode = this.pubsuberManager.get(
                Constants.PUBSUB_NOTIFICATION_EXCHANGE_CHANNEL, Node.class);
            return this.notificationExchangeNode;
        }
    }

    public synchronized void updateNotificationNode() {
        this.notificationExchangeNode = null;
        this.getNotificationExchangeNode();
    }

    public synchronized NodeView getNodeView(String secret) {
        if (Strings.isNullOrEmpty(secret)) {
            throw new NullPointerException("the secret can not be null or empty");
        }

        if (this.secretNodeViewMap.containsKey(secret)) {   //local cache
            return this.secretNodeViewMap.get(secret);
        } else {                                            //remote data then local cache
            NodeView nodeViewObj = this.pubsuberManager.get(secret, NodeView.class);
            this.secretNodeViewMap.put(secret, nodeViewObj);
            return nodeViewObj;
        }
    }

    public synchronized void updateNodeView(String secret) {
        if (this.secretNodeViewMap.containsKey(secret)) {
            this.secretNodeViewMap.remove(secret);
            this.getNodeView(secret);
        }
    }

}
