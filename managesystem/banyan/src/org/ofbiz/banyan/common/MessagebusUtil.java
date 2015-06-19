package org.ofbiz.banyan.common;

import com.google.common.base.Strings;
import com.messagebus.client.model.Node;
import com.messagebus.client.model.NodeView;
import com.messagebus.interactor.pubsub.PubsuberManager;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.cache.UtilCache;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;

import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by yanghua on 4/7/15.
 */
public class MessagebusUtil {

    public static final String module = MessagebusUtil.class.getName();

    public static NodeView buildNodeViewBySecret(Node aQueue, Delegator delegator, Map<String, Node> idNodeMapView) {
        List<GenericValue> sinks = findSinksByFlowFrom(delegator, aQueue.getNodeId());
        List<String> sinkTokens = extractSinkTokens(sinks);
        Map<String, Node> relatedQueueNameNodeMap = buildRelatedNameNodeMapView(sinks, idNodeMapView);
        List<Node> subscribedNodes = filterSubscribeNodes(sinks, idNodeMapView);

        NodeView nodeView = new NodeView();
        nodeView.setSecret(aQueue.getSecret());
        nodeView.setCurrentQueue(aQueue);
        nodeView.setSinkTokens(sinkTokens);
        nodeView.setRelatedQueueNameNodeMap(relatedQueueNameNodeMap);
        nodeView.setSubscribeNodes(subscribedNodes);

        return nodeView;
    }

    public static void publishAndCacheForNodeView(GenericValue nodeEntity, Delegator delegator) {
        MessagebusUtil.publishEvent(com.messagebus.common.Constants.PUBSUB_NODEVIEW_CHANNEL,
                                    nodeEntity.getString("secret"),
                                    false);

        UtilCache<String, Object> poolUtilCache = UtilCache.findCache(Constants.KEY_OF_BANYAN_GLOBAL_CACHE);
        PubsuberManager pubsuberManager = (PubsuberManager) poolUtilCache.get(Constants.KEY_OF_MESSAGEBUS_PUBSUBER_MANAGER);

        Map<String, Node> idNodeViewMap = MessagebusUtil.buildIdNodeMapView(delegator);
        Node node = MessagebusUtil.entityNodeMapping(nodeEntity);
        NodeView newNodeView = MessagebusUtil.buildNodeViewBySecret(node, delegator, idNodeViewMap);
        pubsuberManager.set(nodeEntity.getString("secret"), newNodeView);
    }

    public static Map<String, Node> buildIdNodeMapView(Delegator delegator) {
        List<Node> allPublicQueues = findAllPublicQueues(delegator);
        if (allPublicQueues == null || allPublicQueues.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Node> idNodeMap = new HashMap<>(allPublicQueues.size());
        for (Node node : allPublicQueues) {
            idNodeMap.put(node.getNodeId(), node);
        }

        return idNodeMap;
    }

    public static Node entityNodeMapping(GenericValue entity) {
        Node aQueue = new Node();
        aQueue.setNodeId(entity.getString("nodeId"));
        aQueue.setSecret(entity.getString("secret"));
        aQueue.setName(entity.getString("name"));
        aQueue.setValue(entity.getString("value"));
        aQueue.setType(entity.getString("type"));
        aQueue.setParentId(entity.getString("parentId"));
        aQueue.setRoutingKey(entity.getString("routingKey") == null ? "" : entity.getString("routingKey"));
        aQueue.setRouterType(entity.getString("routerType"));
        aQueue.setAppId(entity.getString("appId"));
        aQueue.setAvailable(entity.getString("available") == null || entity.getString("available").equals("1"));
        aQueue.setInner(entity.getString("isInner") == null || entity.getString("isInner").equals("1"));
        aQueue.setVirtual(entity.getString("isVirtual") == null || entity.getString("isVirtual").equals("1"));
        aQueue.setCanBroadcast(entity.getString("canBroadcast") != null && entity.getString("canBroadcast").equals("1"));
        aQueue.setCommunicateType(entity.getString("communicateType"));
        aQueue.setRateLimit(entity.getString("rateLimit"));
        aQueue.setThreshold(entity.getString("threshold"));
        aQueue.setMsgBodySize(entity.getString("msgBodySize"));
        aQueue.setTtl(entity.getString("ttl"));
        aQueue.setTtlPerMsg(entity.getString("ttlPerMsg"));
        aQueue.setCompress(entity.getString("compress"));

        return aQueue;
    }

    public static void publishEvent(String channel, String content, boolean setByHand) {
        UtilCache<String, Object> poolCache = UtilCache.findCache(Constants.KEY_OF_BANYAN_GLOBAL_CACHE);
        PubsuberManager pubsuberManager = (PubsuberManager) poolCache.get(Constants.KEY_OF_MESSAGEBUS_PUBSUBER_MANAGER);
        if (setByHand) {
            pubsuberManager.set(channel, content.getBytes(Charset.defaultCharset()));
        }

        pubsuberManager.publish(channel, content.getBytes(Charset.defaultCharset()));
    }

    /**
     * public : type : queue; available : true ; audited : true
     *
     * @return
     */
    private static List<Node> findAllPublicQueues(Delegator delegator) {
        EntityCondition whereCondition = EntityCondition.makeCondition(UtilMisc.toMap(
            "type", "1",
            "available", "1",
            "auditTypeCode", Constants.CODE_OF_AUDIT_TYPE_SUCCESS));

        try {
            List<GenericValue> publicQueueGenericValues = delegator.findList("Node", whereCondition, null, null, null, false);
            Debug.logInfo(" public queues generic value size : " + publicQueueGenericValues.size(), module);
            List<Node> publicQueues = new ArrayList<>(publicQueueGenericValues.size());

            for (GenericValue queueEntity : publicQueueGenericValues) {
                publicQueues.add(entityNodeMapping(queueEntity));
            }

            return publicQueues;
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return Collections.emptyList();
        }
    }

    private static Map<String, Node> buildRelatedNameNodeMapView(List<GenericValue> sinks,
                                                                 Map<String, Node> idNodeViewMap) {
        Map<String, Node> relatedNameNodeMap = new HashMap<>(sinks.size());
        for (GenericValue aSink : sinks) {
            String flowTo = aSink.getString("flowTo");
            Node flowToNode = idNodeViewMap.get(flowTo);
            relatedNameNodeMap.put(flowToNode.getName(), flowToNode);
        }

        return relatedNameNodeMap;
    }

    private static List<Node> filterSubscribeNodes(List<GenericValue> sinks,
                                                   Map<String, Node> idNodeViewMap) {
        List<Node> subscribeNodes = new ArrayList<>();
        for (GenericValue aSink : sinks) {
            String fromCommunicateType = aSink.getString("fromCommunicateType");
            String toCommunicateType = aSink.getString("toCommunicateType");

            if (Strings.isNullOrEmpty(fromCommunicateType)) continue;
            if (Strings.isNullOrEmpty(toCommunicateType)) continue;

            if (!fromCommunicateType.equals("publish") && !fromCommunicateType.equals("publish-subscribe"))
                continue;
            if (!toCommunicateType.equals("subscribe") && !toCommunicateType.equals("publish-subscribe")) continue;

            String subscribeNodeId = aSink.getString("flowTo");
            subscribeNodes.add(idNodeViewMap.get(subscribeNodeId));
        }

        return subscribeNodes;
    }

    private static List<GenericValue> findSinksByFlowFrom(Delegator delegator, String flowFrom) {
        EntityCondition whereCondition = EntityCondition.makeCondition(UtilMisc.toMap(
            "flowFrom", flowFrom,
            "auditTypeCode", Constants.CODE_OF_AUDIT_TYPE_SUCCESS));
        try {
            List<GenericValue> sinks = delegator.findList("Sink", whereCondition, null, null, null, false);

            return sinks;
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return Collections.emptyList();
        }
    }

    private static List<String> extractSinkTokens(List<GenericValue> sinks) {
        List<String> sinkTokens = new ArrayList<>(sinks.size());
        for (GenericValue aSink : sinks) {
            sinkTokens.add(aSink.getString("token"));
        }

        return sinkTokens;
    }


}
