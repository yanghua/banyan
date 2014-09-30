package com.freedom.messagebus.client.core.config;

import com.freedom.messagebus.client.core.classLoader.RemoteClassLoader;
import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.model.HandlerModel;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.common.CONSTS;
import com.freedom.messagebus.common.model.Node;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * the config manager
 */
public class ConfigManager {

    private static final Log logger = LogFactory.getLog(ConfigManager.class);

    private boolean inited = false;
    public static volatile ConfigManager instance;

    @NotNull
    private Properties configProperty;
    @NotNull
    private Properties poolProperties;

    //region handle models
    @NotNull
    private List<HandlerModel>    produceHandlerModels;
    @NotNull
    private List<HandlerModel>    consumerHandlerModels;
    @NotNull
    private List<HandlerModel>    requestHandlerModels;
    @NotNull
    private List<HandlerModel>    responseHandlerModels;
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
    //endregion

    @NotNull
    private Map<String, Node> exchangeNodeMap;
    @NotNull
    private Map<String, Node> queueNodeMap;

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

    public Properties getConfigProperty() {
        if (!this.inited)
            throw new IllegalStateException("ZookeeperManager inited failed");

        return configProperty;
    }

    public Properties getPoolProperties() {
        if (!this.inited)
            throw new IllegalStateException("ZookeeperManager inited failed");

        return poolProperties;
    }

    private boolean init() {
        try {
            configProperty = new Properties();
            poolProperties = new Properties();

            configProperty.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));
            poolProperties.load(this.getClass().getClassLoader().getResourceAsStream("pool.properties"));

            //parse
            produceHandlerModels = parseHandlers("produce");
            consumerHandlerModels = parseHandlers("consumer");
            requestHandlerModels = parseHandlers("request");
            responseHandlerModels = parseHandlers("response");

            //box
            produceHandlerChain = initHandlers(MessageCarryType.PRODUCE);
            consumerHandlerChain = initHandlers(MessageCarryType.CONSUME);
            requestHandlerChain = initHandlers(MessageCarryType.REQUEST);
            responseHandlerChain = initHandlers(MessageCarryType.RESPONSE);

            if (logger.isInfoEnabled()) {
                printHandlerChain(MessageCarryType.PRODUCE);
                printHandlerChain(MessageCarryType.CONSUME);
            }

            this.parseRouterInfo();

            return true;
        } catch (IOException e) {
            logger.error("[init] occurs a IOException : " + e.getMessage());
            return false;
        }
    }

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
    public Map<String, Node> getExchangeNodeMap() {
        return exchangeNodeMap;
    }

    @NotNull
    public Map<String, Node> getQueueNodeMap() {
        return queueNodeMap;
    }

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

            default: {
                logger.error("unknown message handle type");
                handlerModels = new ArrayList<HandlerModel>(0);
            }
        }

        logger.info("==============" + title + "=============");
        for (HandlerModel model : handlerModels) {
            logger.info("              " + model.getHandlerName() + "              ");
            logger.info("                     ||                     ");
            logger.info("                     \\/                     ");
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

    public synchronized void parseRouterInfo() throws MalformedURLException {
        SAXReader reader = new SAXReader();
        File routerFile = new File(CONSTS.EXPORTED_NODE_FILE_PATH);
        URL url = routerFile.toURI().toURL();
        Document doc = null;

        try {
            doc = reader.read(url);
        } catch (DocumentException e) {
            logger.error("[parseRule] occurs a DocumentException exception : " + e.getMessage());
        }

        Element rootElement = doc.getRootElement();
        org.dom4j.Node databaseNode = rootElement.selectSingleNode("./database");

        List<Element> rowElements = databaseNode.selectNodes("//row");
        List<Node> nodes = new ArrayList<>(rowElements.size());
        for (Element row : rowElements) {
            Node anode = new Node();

            anode.setGeneratedId(Integer.valueOf(row.selectSingleNode("field[@name='generatedId']").getStringValue()));
            anode.setName(row.selectSingleNode("field[@name='name']").getStringValue());
            anode.setValue(row.selectSingleNode("field[@name='value']").getStringValue());
            anode.setParentId(Integer.valueOf(row.selectSingleNode("field[@name='parentId']").getStringValue()));
            anode.setType(Short.valueOf(row.selectSingleNode("field[@name='type']").getStringValue()));
            anode.setLevel(Short.valueOf(row.selectSingleNode("field[@name='level']").getStringValue()));
            anode.setRouterType(row.selectSingleNode("field[@name='routerType']").getStringValue());
            anode.setRoutingKey(row.selectSingleNode("field[@name='routingKey']").getStringValue());

            nodes.add(anode);
        }

        this.extractDifferentNodes(nodes);
    }

    private void extractDifferentNodes(List<Node> nodes) {
        this.exchangeNodeMap = new ConcurrentHashMap<>();
        this.queueNodeMap = new ConcurrentHashMap<>();

        for (Node node : nodes) {
            if (node.getType() == 0)
                this.exchangeNodeMap.put(node.getName(), node);
            else
                this.queueNodeMap.put(node.getName(), node);
        }
    }
}
