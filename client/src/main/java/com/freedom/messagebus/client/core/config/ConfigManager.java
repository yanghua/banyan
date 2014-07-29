package com.freedom.messagebus.client.core.config;

import com.freedom.messagebus.client.handler.AbstractHandler;
import com.freedom.messagebus.client.model.HandlerModel;
import com.freedom.messagebus.client.model.MessageCarryType;
import com.freedom.messagebus.client.model.RuleModel;
import com.freedom.messagebus.client.model.RuleType;
import com.freedom.messagebus.common.CommonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * the config manager
 */
public class ConfigManager {

    private static final Log logger = LogFactory.getLog(ConfigManager.class);

    @NotNull
    private ZooKeeper zooKeeper;

    @NotNull
    private Properties configProperty;
    @NotNull
    private Properties poolProperties;

    @NotNull
    private List<HandlerModel>    produceHandlerModels;
    @NotNull
    private List<HandlerModel>    consumerHandlerModels;
    @NotNull
    private List<AbstractHandler> produceHandlerChain;
    @NotNull
    private List<AbstractHandler> consumerHandlerChain;

    @NotNull
    private Map<String, RuleModel> routingKeyRules;
    @NotNull
    private Map<String, RuleModel> queueNameRules;

    private List<String> paths;

    private boolean inited = false;

    public static volatile ConfigManager instance;

    private ConfigManager(@NotNull ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
        this.inited = this.init();
    }

    @NotNull
    public static ConfigManager getInstance(@NotNull ZooKeeper zooKeeper) {
        if (instance == null) {
            synchronized (ConfigManager.class) {
                if (instance == null) {
                    instance = new ConfigManager(zooKeeper);
                }
            }
        }

        return instance;
    }

    public Properties getConfigProperty() {
        if (!this.inited)
            throw new IllegalStateException("ConfigManager inited failed");

        return configProperty;
    }

    public Properties getPoolProperties() {
        if (!this.inited)
            throw new IllegalStateException("ConfigManager inited failed");

        return poolProperties;
    }

    public List<String> getPaths() {
        return paths;
    }

    private boolean init() {
        try {
            configProperty = new Properties();
            poolProperties = new Properties();

            //TODO:test
            File file = new File((this.getClass().getClassLoader().getResource("pool.properties")).toURI());
            byte[] fileBytes = CommonUtil.getBytesFromFile(file);
            zooKeeper.setData("/proxy", fileBytes, 19);

            byte[] poolData = zooKeeper.getData("/proxy", false, null);
            ByteArrayInputStream bais = new ByteArrayInputStream(poolData);
            poolProperties.load(bais);

            configProperty.load(this.getClass().getClassLoader().getResourceAsStream("config.properties"));
//            poolProperties.load(this.getClass().getClassLoader().getResourceAsStream("pool.properties"));

            //parse
            List<HandlerModel> pHandlerModels = parseHandlers("produce");
            List<HandlerModel> cHandlerModels = parseHandlers("consumer");

            //changed to unmodifiable
            produceHandlerModels = Collections.unmodifiableList(pHandlerModels);
            consumerHandlerModels = Collections.unmodifiableList(cHandlerModels);

            //box
            List<AbstractHandler> pHandlerChain = initHandlers(MessageCarryType.PRODUCE);
            List<AbstractHandler> cHandlerChain = initHandlers(MessageCarryType.CONSUME);

            //changed to unmodifiable
            produceHandlerChain = Collections.unmodifiableList(pHandlerChain);
            consumerHandlerChain = Collections.unmodifiableList(cHandlerChain);

            if (logger.isInfoEnabled()) {
                printHandlerChain(MessageCarryType.PRODUCE);
                printHandlerChain(MessageCarryType.CONSUME);
            }

            //parse rule
            Map<String, RuleModel> rRules = parseRule(RuleType.ROUTINGKEY);
            Map<String, RuleModel> qRules = parseRule(RuleType.QUEUENAME);

            //changed to unmodifiable
            routingKeyRules = Collections.unmodifiableMap(rRules);
            queueNameRules = Collections.unmodifiableMap(qRules);

            //parse path
            paths = parsePath();

            return true;
        } catch (IOException e) {
            logger.error("[init] occurs a IOException : " + e.getMessage());
            return false;
        } catch (KeeperException e) {
            logger.error("[init] occurs a KeeperException : " + e.getMessage());
            return false;
        } catch (InterruptedException e) {
            logger.error("[init] occurs a InterruptedException : " + e.getMessage());
            return false;
        } catch (URISyntaxException e) {
            logger.error("[init] occurs a URISyntaxException : " + e.getMessage());
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
    public List<AbstractHandler> getProduceHandlerChain() {
        return produceHandlerChain;
    }

    @NotNull
    public List<AbstractHandler> getConsumerHandlerChain() {
        return consumerHandlerChain;
    }

    @NotNull
    public Map<String, RuleModel> getRoutingKeyRules() {
        return routingKeyRules;
    }

    @NotNull
    public Map<String, RuleModel> getQueueNameRules() {
        return queueNameRules;
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

            Node handlerNameNode = xPath.selectSingleNode(element);
            String handlerName = handlerNameNode.getStringValue();

            xPath = element.createXPath("ns:handler-path");
            xPath.setNamespaceURIs(map);

            Node handlerPathNode = xPath.selectSingleNode(element);
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

    public void destroy() {
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

    @NotNull
    private Map<String, RuleModel> parseRule(RuleType ruleType) {
        Map<String, RuleModel> result = new HashMap<String, RuleModel>();

        SAXReader reader = new SAXReader();
        URL url = ConfigManager.class.getClassLoader().getResource("rule.xml");
        Document doc = null;
        String nsPartialStr = "";

        switch (ruleType) {
            case ROUTINGKEY:
                nsPartialStr = "routingKeys";
                break;

            case QUEUENAME:
                nsPartialStr = "queueNames";
                break;
        }

        try {
            doc = reader.read(url);
        } catch (DocumentException e) {
            logger.error("[parseRule] occurs a DocumentException exception : " + e.getMessage());
        }

        Map<String, String> map = new HashMap<String, String>();
        map.put("ns", "http://com.freedom.messagebus");
        XPath xPath = doc.createXPath("//ns:rules/ns:" + nsPartialStr + "/ns:rule");
        xPath.setNamespaceURIs(map);

        List<Element> ruleElements = xPath.selectNodes(doc);

        for (Element ruleElement : ruleElements) {
            xPath = ruleElement.createXPath("ns:rule-name");
            xPath.setNamespaceURIs(map);

            Node ruleNameNode = xPath.selectSingleNode(ruleElement);
            String ruleName = ruleNameNode.getStringValue();

            xPath = ruleElement.createXPath("ns:rule-pattern");
            xPath.setNamespaceURIs(map);

            Node rulePatternNode = xPath.selectSingleNode(ruleElement);
            String rulePattern = rulePatternNode.getStringValue();

            RuleModel ruleModel = new RuleModel();
            ruleModel.setRuleName(ruleName);
            ruleModel.setRulePattern(rulePattern);

            result.put(ruleModel.getRuleName(), ruleModel);
        }

        return result;
    }

    @NotNull
    private List<String> parsePath() {
        List<String> result = new ArrayList<String>();

        SAXReader reader = new SAXReader();
        URL url = ConfigManager.class.getClassLoader().getResource("path.xml");
        Document doc = null;

        try {
            doc = reader.read(url);
        } catch (DocumentException e) {
            logger.error("[parseRule] occurs a DocumentException exception : " + e.getMessage());
        }

        Map<String, String> nsmap = new HashMap<String, String>();
        nsmap.put("ns", "http://com.freedom.messagebus");
        String rootXPathStr = "//ns:path";
        XPath xPath = doc.createXPath(rootXPathStr);
        xPath.setNamespaceURIs(nsmap);

        List<Element> paths = xPath.selectNodes(doc);

        for (Element e : paths) {
            result.add(e.attributeValue("p"));
        }

        return result;
    }

}
