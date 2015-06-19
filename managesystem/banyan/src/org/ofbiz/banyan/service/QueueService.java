package org.ofbiz.banyan.service;

import com.messagebus.common.RandomHelper;
import org.ofbiz.banyan.common.Constants;
import org.ofbiz.banyan.common.MessagebusUtil;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.Delegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityJoinOperator;
import org.ofbiz.entity.util.EntityFindOptions;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

import java.lang.Exception;
import java.lang.RuntimeException;
import java.lang.String;
import java.util.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by yanghua on 3/20/15.
 */
public class QueueService {

    public static final String module        = QueueService.class.getName();
    public static final String resourceError = "BanyanUiLabels";

    private static final String proconRoutingKeyPrefix  = "queue.proxy.message.procon.";
    private static final String reqrespRoutingKeyPrefix = "queue.proxy.message.reqresp.";
    private static final String pubsubRoutingKeyPrefix  = "queue.proxy.message.pubsub.";
    private static final String rpcRoutingKeyPrefix     = "queue.proxy.message.rpc";

    private static final String proconParentId  = "4";
    private static final String pubsubParentId  = "5";
    private static final String reqrespParentId = "6";
    private static final String rpcParentId     = "7";

    public static Map<String, Object> createQueue(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        Locale locale = (Locale) context.get("locale");
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        Debug.logInfo("param : name is null : " + (context.get("name") == null), module);

        String nodeName = (String) context.get("name");
        String carryTypeValue = (String) context.get("communicateType");

        //check exists node name
        try {
            EntityCondition whereCondition = EntityCondition.makeCondition(UtilMisc.toMap("name", nodeName));
            long count = delegator.findCountByCondition("Node", whereCondition, null, null);
            if (count > 0) {
                return ServiceUtil.returnError("the node with name : " + nodeName + " is exists ");
            }
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError(e.getMessage());
        }

        String nodeValue;
        String parentId;
        String isVirtual = "0";

        switch (carryTypeValue) {
            case "produce":
            case "consume":
            case "produce-consume":
                nodeValue = proconRoutingKeyPrefix + nodeName;
                parentId = proconParentId;
                break;


            case "publish":
            case "subscribe":
            case "publish-subscribe":
                nodeValue = pubsubRoutingKeyPrefix + nodeName;
                parentId = pubsubParentId;
                break;

            case "request":
            case "response":
            case "request-response":
                nodeValue = reqrespRoutingKeyPrefix + nodeName;
                parentId = reqrespParentId;
                break;

            case "rpcrequest":
            case "rpcresponse":
            case "rpcrequest-rpcresponse":
                nodeValue = rpcRoutingKeyPrefix + nodeName;
                parentId = rpcParentId;
                break;

            default:
                Debug.logError("unknown carry type value : " + carryTypeValue, module);
                return ServiceUtil.returnError("unknown carry type value : " + carryTypeValue);
        }

        switch (carryTypeValue) {
            case "produce":
            case "publish":
            case "rpcrequest":
                isVirtual = "1";
                break;

            case "consume":
            case "subscribe":
            case "request":
            case "response":
            case "rpcresponse":
            case "produce-consume":
            case "publish-subscribe":
            case "request-response":
                isVirtual = "0";
                break;
        }

        GenericValue node = delegator.makeValue("Node");

        node.setString("nodeId", delegator.getNextSeqId("Node"));
        node.setString("secret", RandomHelper.randomNumberAndCharacter(20));
        node.setString("name", nodeName);
        node.setString("value", nodeValue);
        node.setString("appId", context.get("appId").toString());
        node.setString("parentId", parentId);
        node.setString("type", "1");
        node.setString("routerType", "");
        node.setString("routingKey", nodeValue.replace("queue", "routingkey"));
        node.setString("available", "1");
        node.setString("isInner", "0");
        node.setString("isVirtual", isVirtual);
        node.setString("communicateType", carryTypeValue);
        node.setString("creator", userLogin.getString("userLoginId"));
        node.setString("auditTypeCode", Constants.CODE_OF_AUDIT_TYPE_UNAUDIT);
        node.set("rateLimit", context.get("rateLimit"));
        node.set("threshold", context.get("threshold"));
        node.set("msgBodySize", context.get("msgBodySize"));
        node.set("ttl", context.get("ttl"));
        node.set("ttlPerMsg", context.get("ttlPerMsg"));
        node.set("canBroadcast", context.get("canBroadcast"));
        node.set("description", context.get("description"));
        Debug.log("the compress field is : " + context.get("compress"), module);
        node.set("compress", context.get("compress"));

        try {
            delegator.create(node);
            MessagebusUtil.publishAndCacheForNodeView(node, delegator);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object> updateQueue(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();

        String nodeId = (String) context.get("nodeId");
        String nodeName = (String) context.get("name");
        String available = (String) context.get("available");
        String canBroadcast = (String) context.get("canBroadcast");
        String description = (String) context.get("description");

        try {
            //check exists node name
            EntityCondition oneCondition = EntityCondition.makeCondition(UtilMisc.toMap("name", nodeName));
            EntityCondition anotherCondition = EntityCondition.makeConditionWhere(" NODE_ID != '" + nodeId + "'");

            EntityCondition whereCondition = EntityCondition.makeCondition(oneCondition,
                                                                           EntityJoinOperator.AND,
                                                                           anotherCondition);

            long count = delegator.findCountByCondition("Node", whereCondition, null, null);
            if (count > 0) {
                return ServiceUtil.returnError("the node with name : " + nodeName + " is exists ");
            }

            GenericValue queue = delegator.findOne("Node", UtilMisc.toMap("nodeId", nodeId), false);
            String oldName = queue.getString("name");
            String oldVal = queue.getString("value");
            String oldRoutingKey = queue.getString("routingKey");

            queue.set("name", nodeName);
            queue.set("value", oldVal.replace(oldName, nodeName));
            queue.set("routingKey", oldRoutingKey.replace(oldName, nodeName));
            queue.set("available", available);
            queue.set("canBroadcast", canBroadcast);
            queue.set("description", description);

            delegator.store(queue, true);

            MessagebusUtil.publishAndCacheForNodeView(queue, delegator);

            return ServiceUtil.returnSuccess();
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        } catch (Exception e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
    }

    public static Map<String, Object> auditQueue(DispatchContext ctx, Map<String, ? extends Object> context) {
        String queueId = (String) context.get("queueId");
        Delegator delegator = ctx.getDelegator();

        try {
            GenericValue queueInfo = delegator.findOne("Node", UtilMisc.toMap("nodeId", queueId), false);
            queueInfo.setString("auditTypeCode", Constants.CODE_OF_AUDIT_TYPE_SUCCESS);
            queueInfo.setString("secret", RandomHelper.randomNumberAndCharacter(20));
            delegator.store(queueInfo);

            MessagebusUtil.publishAndCacheForNodeView(queueInfo, delegator);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object> getQueueRateWarningsById(DispatchContext ctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = ctx.getDispatcher();
        try {
            Map<String, Object> resultCtx = dispatcher.runSync("performFind", context);
            return resultCtx;
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
    }

    public static Map<String, Object> getAvailableFlowToQueues(DispatchContext ctx, Map<String, Object> context) {
        LocalDispatcher dispatcher = ctx.getDispatcher();
        Delegator delegator = ctx.getDelegator();
        String flowFromId = (String) context.get("flowFromId");

        if (flowFromId != null && !flowFromId.isEmpty()) {
            //if there is a flowFromId key , it will be failed when exec performFind
            context.remove("flowFromId");
        }

        GenericValue queue = null;
        try {
            queue = delegator.findOne("Node", UtilMisc.toMap("nodeId", flowFromId), false);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        String communicateType = queue.getString("communicateType");

        String flowToCommTypeSubStr = "";
        switch (communicateType) {
            case "produce":
            case "produce-consume":
                flowToCommTypeSubStr = "consume";
                break;

            case "publish":
            case "publish-subscribe":
                flowToCommTypeSubStr = "subscribe";
                break;


            case "request":
            case "request-response":
                flowToCommTypeSubStr = "response";
                break;

            case "rpcrequest":
                flowToCommTypeSubStr = "rpcresponse";
                break;

            default:
                Debug.logError("unknown communicate type : " +
                                   communicateType + "the flow from id is : " + flowFromId, module);
        }

        context.put("entityName", "Node");

        Map<String, Object> inputFields = new HashMap<>();

        inputFields.put("appId_op", "notEqual");
        inputFields.put("appId", flowFromId);
        inputFields.put("appId_ic", "Y");

        inputFields.put("communicateType_op", "contains");
        inputFields.put("communicateType", flowToCommTypeSubStr);
        inputFields.put("communicateType_ic", "Y");
        context.put("inputFields", inputFields);

        context.put("noConditionFind", "N");

        try {
            Map<String, Object> resultCtx = dispatcher.runSync("performFindList", context);
            return resultCtx;
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
    }

    public static Map<String, Object> testSoapService(DispatchContext ctx, Map<String, ? extends Object> context) {
        Delegator delegator = ctx.getDelegator();
        try {
            List<GenericValue> queues = delegator.findByAnd("Node", UtilMisc.toMap("type", "1"), null, false);
            Map<String, Object> result = ServiceUtil.returnSuccess();
            result.put("results", queues);
            return result;
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            Map<String, Object> result = ServiceUtil.returnError(e.getMessage());
            result.put("results", Collections.emptyList());
            return result;
        }
    }

}
