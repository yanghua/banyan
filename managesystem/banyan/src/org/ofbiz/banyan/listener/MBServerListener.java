package org.ofbiz.banyan.listener;

import com.messagebus.client.MessagebusPool;
import com.messagebus.client.model.Node;
import com.messagebus.client.model.NodeView;
import com.messagebus.interactor.pubsub.PubsuberManager;
import com.messagebus.service.bootstrap.LogConfigInitializer;
import com.messagebus.service.bootstrap.MQDataInitializer;
import com.messagebus.service.daemon.IService;
import com.messagebus.service.daemon.IServiceCallback;
import com.messagebus.service.daemon.ServiceLoader;
import com.messagebus.service.daemon.impl.RateWarningMonitorService;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.ofbiz.banyan.common.Constants;
import org.ofbiz.banyan.common.MessagebusUtil;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.cache.UtilCache;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.DelegatorFactory;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityJoinOperator;
import org.ofbiz.entity.condition.EntityOperator;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by yanghua on 4/13/15.
 */
public class MBServerListener implements ServletContextListener {

    public static String module = MBServerListener.class.getName();

    private MessagebusPool innerMessagebusPool;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        String delegatorName = servletContext.getInitParameter("entityDelegatorName");
        final Delegator delegator = DelegatorFactory.getDelegator(delegatorName);
        Debug.log(" is delegator null : " + (delegator == null));

        Debug.logInfo("=-=-=-=-=-=-=-=-=-=-=-=-=-=bootstrap service (start)-=-=-=-=-=-=-=-=-=-=-=-=-=-", module);

        //start up bootstrap service
        Debug.logInfo("initializing log config file....", module);
        String logConfigFilePath = UtilProperties.getPropertyValue("MessagebusConfig",
                                                                   "messagebus.service.log4jPropertiesPath");
        LogConfigInitializer logConfigInitializer = LogConfigInitializer.defaultConfigInitializer(logConfigFilePath);
        logConfigInitializer.launch();

        Debug.logInfo("initializing mq server.... ", module);
        String mqHost = UtilProperties.getPropertyValue("MessagebusConfig", "messagebus.mq.host");
        Debug.logInfo("message queue server host is : " + mqHost, module);
        MQDataInitializer mqManager = MQDataInitializer.getInstance(mqHost);
        List<Node> allNodes = findAllNode(delegator);
        Debug.logInfo("all nodes count : " + allNodes.size(), module);
        try {
            mqManager.initTopologyComponent(allNodes.toArray(new Node[allNodes.size()]));
        } catch (IOException e) {
            Debug.logError(e, module);
            throw new RuntimeException("init mq topology component error : " + e.getMessage());
        }

        //refresh mq host config
        Debug.logInfo("refreshing config for mq host with new key : " + mqHost, module);
        try {
            List<GenericValue> results = delegator.findByAnd("Config", UtilMisc.toMap("itemKey", "messagebus.client.host"), null, false);
            GenericValue oldConfigItem = results.get(0);
            oldConfigItem.setString("itemValue", mqHost);
            delegator.store(oldConfigItem);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            throw new RuntimeException("refresh config with key 'messagebus.client.host' error " + e.getMessage());
        }

        //cache mete data
        Debug.logInfo("caching meta data to pubsuber.... ", module);
        String pubsuberHost = UtilProperties.getPropertyValue("MessagebusConfig", "messagebus.pubsuberHost");
        int pubsuberPort = Integer.parseInt(UtilProperties.getPropertyValue("MessagebusConfig", "messagebus.pubsuberPort"));
        Debug.logInfo("pubsuber host : " + pubsuberHost, module);
        Debug.logInfo("pubsuber port : " + pubsuberPort, module);
        PubsuberManager pubsuberManager = new PubsuberManager(pubsuberHost, pubsuberPort);

        //cache pubsuberManager
        UtilCache<String, Object> banyanGlobalCache = UtilCache.createUtilCache(Constants.KEY_OF_BANYAN_GLOBAL_CACHE);
        banyanGlobalCache.put(Constants.KEY_OF_MESSAGEBUS_PUBSUBER_MANAGER, pubsuberManager);
        cacheMetaData(pubsuberManager, delegator, allNodes);

        Debug.logInfo("=-=-=-=-=-=-=-=-=-=-=-=-=-=bootstrap service (end)-=-=-=-=-=-=-=-=-=-=-=-=-=-", module);

        //broadcast start event
        Debug.logInfo("publishing server started event ...", module);
        pubsuberManager.publish(com.messagebus.common.Constants.PUBSUB_SERVER_STATE_CHANNEL,
                                com.messagebus.common.Constants.MESSAGEBUS_SERVER_EVENT_STARTED.getBytes(), true);

        Debug.logInfo("initial a inner messagebus client pool ...", module);
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(10);
        innerMessagebusPool = new MessagebusPool(pubsuberHost, pubsuberPort, poolConfig);

        //start up daemon service
        Debug.logInfo("=-=-=-=-=-=-=-=-=-=-=-=-=-=daemon service (start)-=-=-=-=-=-=-=-=-=-=-=-=-=-", module);
        Map<String, Object> ctx = new HashMap<String, Object>(2);
        ctx.put(com.messagebus.service.Constants.GLOBAL_CLIENT_POOL, innerMessagebusPool);
        ctx.put(com.messagebus.service.Constants.MQ_HOST_KEY, mqHost);
        ServiceLoader serviceLoader = ServiceLoader.getInstance(ctx);

        Debug.logInfo("initializing deamon service : rateWarningMonitorService ...", module);
        final List<GenericValue> rateLimitedQueues = this.findAllRateLimitedQueues(delegator);
        IService rateWarningMonitorService = new RateWarningMonitorService(ctx);
        ((RateWarningMonitorService) rateWarningMonitorService).setCallback(new IServiceCallback() {
            @Override
            public void callback(Map<String, Object> map) {
                List<Object> remoteObjs = (List<Object>) map.get("queueInfoList");
                try {
                    for (GenericValue queue : rateLimitedQueues) {
                        String queueName = queue.getString("value");
                        for (Object queueInfoObj : remoteObjs) {
                            Map<String, Object> queueInfo = (Map) queueInfoObj;
                            if (queueInfo.get("name").equals(queueName)) {
                                Map<String, Object> msgStatsInfo = (Map) queueInfoObj;
                                Map<String, Object> publishDetailMap = (Map) msgStatsInfo.get("publish_details");
                                int benchmark = Integer.parseInt(queue.getString("rateLimit"));
                                int realRate = Integer.parseInt(publishDetailMap.get("rate").toString());
                                //log to rate limit
                                if (realRate > benchmark) {
                                    GenericValue nodeEntity = delegator.makeValue("Node");
                                    nodeEntity.setString("warningId", delegator.getNextSeqId("QueueRateWarning"));
                                    nodeEntity.setString("nodeId", queue.getString("nodeId"));
                                    nodeEntity.setString("rateLimit", queue.getString("rateLimit"));
                                    nodeEntity.setString("realRate", publishDetailMap.get("rate").toString());
                                    nodeEntity.set("fromDate", UtilDateTime.nowDate());
                                    delegator.create(nodeEntity);
                                }
                            }
                        }
                    }
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                }
            }
        });

        serviceLoader.getScheduleCycleServiceMap().put("rateWarningMonitorService", rateWarningMonitorService);
        serviceLoader.launch();
        Debug.logInfo("=-=-=-=-=-=-=-=-=-=-=-=-=-=daemon service (end)-=-=-=-=-=-=-=-=-=-=-=-=-=-", module);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        Debug.logInfo("messagebus server is stopping...", module);
        if (innerMessagebusPool != null) innerMessagebusPool.destroy();

        UtilCache<String, Object> poolUtilCache = UtilCache.findCache(Constants.KEY_OF_BANYAN_GLOBAL_CACHE);
        PubsuberManager pubsuberManager = (PubsuberManager) poolUtilCache.get(Constants.KEY_OF_MESSAGEBUS_PUBSUBER_MANAGER);

        Debug.logInfo("publishing server started event ...", module);
        pubsuberManager.publish(com.messagebus.common.Constants.PUBSUB_SERVER_STATE_CHANNEL,
                                com.messagebus.common.Constants.MESSAGEBUS_SERVER_EVENT_STOPPED.getBytes(), true);

        poolUtilCache.clear();
        Debug.logInfo("messagebus server stopped.", module);
    }

    private List<Node> findAllNode(Delegator delegator) {
        List<String> orderByList = new ArrayList<String>(1);
        orderByList.add("+parentId");
        List<GenericValue> nodeList = null;
        try {
            nodeList = delegator.findList("Node", null, null, orderByList, null, false);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return Collections.emptyList();
        }

        if (nodeList == null || nodeList.size() == 0) {
            return Collections.emptyList();
        }

        Iterator<GenericValue> iterator = nodeList.iterator();
        List<Node> nodes = new ArrayList<>(nodeList.size());
        while (iterator.hasNext()) {
            GenericValue genericNode = iterator.next();
            Node node = new Node();
            node.setNodeId(genericNode.getString("nodeId"));
            node.setSecret(genericNode.getString("secret"));
            node.setName(genericNode.getString("name"));
            node.setValue(genericNode.getString("value"));
            node.setType(genericNode.getString("type"));
            node.setParentId(genericNode.getString("parentId"));
            node.setRoutingKey(genericNode.getString("routingKey") == null ? "" : genericNode.getString("routingKey"));
            node.setRouterType(genericNode.getString("routerType"));
            node.setAppId(genericNode.getString("appId"));
            node.setAvailable(genericNode.getString("available") != null
                                  && genericNode.getString("available").equals("1"));
            node.setInner(genericNode.getString("isInner") != null
                              && genericNode.getString("isInner").equals("1"));
            node.setVirtual(genericNode.getString("isVirtual") != null
                                && genericNode.getString("isVirtual").equals("1"));
            node.setCanBroadcast(genericNode.getString("canBroadcast") != null && genericNode.getString("canBroadcast").equals("1"));
            node.setCommunicateType(genericNode.getString("communicateType"));
            node.setRateLimit(genericNode.getString("rateLimit"));
            node.setThreshold(genericNode.getString("threshold"));
            node.setMsgBodySize(genericNode.getString("msgBodySize"));
            node.setTtl(genericNode.getString("ttl"));
            node.setTtlPerMsg(genericNode.getString("ttlPerMsg"));
            nodes.add(node);
        }

        return nodes;
    }

    private void cacheMetaData(PubsuberManager pubsuberManager, Delegator delegator, List<Node> allNode) {
        //cache queue data
        Map<String, NodeView> secretNodeViewMap = this.buildMetaData(delegator);
        Debug.logInfo(" secret node view map num : " + secretNodeViewMap.size(), module);
        for (Map.Entry<String, NodeView> entry : secretNodeViewMap.entrySet()) {
            pubsuberManager.set(entry.getKey(), entry.getValue());
        }

        //cache client config data
        Map<String, String> allClientConfigMap = this.findAllClientConfig(delegator);
        for (Map.Entry<String, String> entry : allClientConfigMap.entrySet()) {
            pubsuberManager.set(entry.getKey(), entry.getValue());
        }

        //cache notification exchange node
        Node notificationNode = this.findNotificationExchangeNode(allNode);
        pubsuberManager.set(com.messagebus.common.Constants.PUBSUB_NOTIFICATION_EXCHANGE_CHANNEL,
                            notificationNode);
        pubsuberManager.publish(com.messagebus.common.Constants.PUBSUB_NOTIFICATION_EXCHANGE_CHANNEL,
                                "".getBytes(Charset.defaultCharset()));
    }

    private Map<String, NodeView> buildMetaData(Delegator delegator) {
        Map<String, Node> idNodeMapView = MessagebusUtil.buildIdNodeMapView(delegator);

        Map<String, NodeView> secretNodeViewMap = new HashMap<>(idNodeMapView.size());
        for (Node aQueue : idNodeMapView.values()) {
            NodeView nodeView = MessagebusUtil.buildNodeViewBySecret(aQueue, delegator, idNodeMapView);

            secretNodeViewMap.put(aQueue.getSecret(), nodeView);
        }

        return secretNodeViewMap;
    }

    private List<GenericValue> findAllRateLimitedQueues(Delegator delegator) {
        EntityCondition publicQueueCondition = EntityCondition.makeCondition(UtilMisc.toMap(
            "type", "1",
            "available", "1",
            "auditTypeCode", Constants.CODE_OF_AUDIT_TYPE_SUCCESS));
        EntityCondition cdn1 = EntityCondition.makeCondition("rateLimit", EntityOperator.NOT_EQUAL, null);
        EntityCondition cdn2 = EntityCondition.makeCondition("rateLimit", EntityOperator.NOT_EQUAL, "");
        List<EntityCondition> conditions = new ArrayList<>(2);
        conditions.add(publicQueueCondition);
        conditions.add(cdn1);
        conditions.add(cdn2);

        EntityCondition joinedCnd = EntityCondition.makeCondition(conditions, EntityJoinOperator.AND);

        Set<String> fieldsToSelected = new HashSet<>(2);
        fieldsToSelected.add("nodeId");
        fieldsToSelected.add("name");
        fieldsToSelected.add("value");
        fieldsToSelected.add("rateLimit");

        try {
            List<GenericValue> rateLimitedQueues = delegator.findList("Node", joinedCnd, fieldsToSelected, null, null, false);

            return rateLimitedQueues;
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return Collections.emptyList();
        }
    }

    private Map<String, String> findAllClientConfig(Delegator delegator) {
        EntityCondition whereCondition = EntityCondition.makeCondition(UtilMisc.toMap(
            "type", "client"));
        try {
            List<GenericValue> clientConfigs = delegator.findList("Config", whereCondition, null, null, null, false);
            Map<String, String> clientConfigMap = new HashMap<>(clientConfigs.size());
            for (GenericValue clientConfig : clientConfigs) {
                clientConfigMap.put(clientConfig.getString("itemKey"), clientConfig.getString("itemValue"));
            }

            return clientConfigMap;
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return Collections.emptyMap();
        }
    }

    private Node findNotificationExchangeNode(List<Node> allNodes) {
        for (Node node : allNodes) {
            if (node.getType().equals("0") && node.getValue().contains("exchange")
                && node.getValue().contains("notification"))
                return node;
        }

        return new Node();
    }

}